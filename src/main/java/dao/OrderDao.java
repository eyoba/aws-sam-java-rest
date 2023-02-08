package dao;

import exception.CouldNotCreateOrderException;
import exception.OrderDoesNotExistException;
import exception.TableDoesNotExistException;
import model.Order;
import model.request.CreateOrderRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.math.BigDecimal;
import java.util.*;

public class OrderDao {

    private static final String UPDATE_EXPRESSION
            = "SET customerId = :cid, preTaxAmount = :pre, postTaxAmount = :post ADD version :o";
    private static final String ORDER_ID = "orderId";
    private static final String PRE_TAX_AMOUNT_WAS_NULL = "preTaxAmount was null";
    private static final String POST_TAX_AMOUNT_WAS_NULL = "postTaxAmount was null";
    private static final String VERSION_WAS_NULL = "version was null";

    private final String tableName;
    private final DynamoDbClient dynamoDb;
    private final int pageSize;

    /**
     * Constructs an OrderDao.
     * @param dynamoDb dynamodb client
     * @param tableName name of table to use for orders
     * @param pageSize size of pages for getOrders
     */
    public OrderDao(final DynamoDbClient dynamoDb, final String tableName,
                    final int pageSize) {
        this.dynamoDb = dynamoDb;
        this.tableName = tableName;
        this.pageSize = pageSize;
    }

    private Map<String, AttributeValue> createOrderItem(final CreateOrderRequest order) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(ORDER_ID, AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        item.put("version", AttributeValue.builder().n("1").build());
        item.put("customerId",
                AttributeValue.builder().s(validateCustomerId(order.getCustomerId())).build());
        try {
            item.put("preTaxAmount",
                    AttributeValue.builder().n(order.getPreTaxAmount().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(PRE_TAX_AMOUNT_WAS_NULL);
        }
        try {
            item.put("postTaxAmount",
                    AttributeValue.builder().n(order.getPostTaxAmount().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(POST_TAX_AMOUNT_WAS_NULL);
        }

        return item;
    }

    private String validateCustomerId(final String customerId) {
        if (isNullOrEmpty(customerId)) {
            throw new IllegalArgumentException("customerId was null or empty");
        }
        return customerId;
    }


    /**
     * Creates an order.
     * @param createOrderRequest details of order to create
     * @return created order
     */
    public Order createOrder(final CreateOrderRequest createOrderRequest) {
        if (createOrderRequest == null) {
            throw new IllegalArgumentException("CreateOrderRequest was null");
        }
        int tries = 0;
        while (tries < 10) {
            try {
                Map<String, AttributeValue> item = createOrderItem(createOrderRequest);
                dynamoDb.putItem(PutItemRequest.builder()
                        .tableName(tableName)
                        .item(item)
                        .conditionExpression("attribute_not_exists(orderId)")
                        .build());
                return Order.builder()
                        .orderId(item.get(ORDER_ID).s())
                        .customerId(item.get("customerId").s())
                        .preTaxAmount(new BigDecimal(item.get("preTaxAmount").n()))
                        .postTaxAmount(new BigDecimal(item.get("postTaxAmount").n()))
                        .version(Long.valueOf(item.get("version").n()))
                        .build();
            } catch (ConditionalCheckFailedException e) {
                tries++;
            } catch (ResourceNotFoundException e) {
                throw new TableDoesNotExistException(
                        "Order table " + tableName + " does not exist");
            }
        }
        throw new CouldNotCreateOrderException(
                "Unable to generate unique order id after 10 tries");
    }

    private static boolean isNullOrEmpty(final String string) {
        return string == null || string.isEmpty();
    }
}
