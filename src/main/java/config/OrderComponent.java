package config;


import dagger.Component;
import helloworld.CreateOrderHandler;

import javax.inject.Singleton;

@Singleton
@Component(modules = {OrderModule.class})
public interface OrderComponent {

    void inject(CreateOrderHandler requestHandler);

   /* void inject(DeleteOrderHandler requestHandler);

    void inject(GetOrderHandler requestHandler);

    void inject(GetOrdersHandler requestHandler);

    void inject(UpdateOrderHandler requestHandler);*/
}
