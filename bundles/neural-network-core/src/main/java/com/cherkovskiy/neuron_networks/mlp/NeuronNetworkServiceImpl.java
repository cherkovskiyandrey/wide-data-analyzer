package com.cherkovskiy.neuron_networks.mlp;

import com.cherkovskiy.application_context.ApplicationContextHolder;
import com.cherkovskiy.application_context.api.ServiceLifecycle;
import com.cherkovskiy.application_context.api.annotations.Service;
import com.cherkovskiy.application_context.api.annotations.ServiceInject;
import com.cherkovskiy.application_context.api.exceptions.ServiceNotFoundException;
import com.cherkovskiy.comprehensive_serializer.api.SerializerService;
import com.cherkovskiy.neuron_networks.api.*;

import javax.annotation.Nonnull;


@Service(
        name = "mlp",
        lifecycleType = Service.LifecycleType.SINGLETON,
        initType = Service.InitType.EAGER
)
public class NeuronNetworkServiceImpl implements NeuronNetworkService, ServiceLifecycle {

    //TODO: or me can use like this
    //private final ApplicationContext applicationContext;
//    public NeuronNetworkServiceImpl(ApplicationContext applicationContext) {
//        this.applicationContext = applicationContext;
//    }

    private final SerializerService serializerService;

    public NeuronNetworkServiceImpl(@ServiceInject SerializerService serializerService) {
        this.serializerService = serializerService;



        //TODO: продумать интерфейс евент системы
//        ApplicationContextHolder.currentContext().registerListener(SerializerService.class, event -> {
//
//            //TODO: реакция на выгрузку бандла с нужным сервисом
//            if (event.getType() == UNLOADED) {
//                try {
//                    //TODO: попытаемся поискать другой бандл с имплементацией этого сервиса
//                    this.serializerService = ApplicationContextHolder.currentContext().getService(SerializerService.class);
//                } catch (ServiceNotFoundException e) {
//                    //TODO: отрегестрируемся особенно если я прототип
//                    ApplicationContextHolder.currentContext().unregisterListener(this); //
//                    //TODO: то я реализовал бы ServiceLifecycle.isValid и вернул бы там false, а фреймворк - попытался бы пересоздать меня
//                    isValid = false;
//                }
//                //TODO: бандл бы перезагружен, или та же версия или более свежая
//            } else if (event.getType() == RELOADED) {
//                //TODO: можно ничего не делать, ссылки в прокси сами поменялись
//            }
//        });
    }

    @Nonnull
    @Override
    public NeuronNetworkBuilder createFeedforwardBuilder() {
//        try {
//            return new FeedforwardNeuronNetworkBuilderImpl(applicationContext.getService(SerializerService.class));
//        } catch (ServiceNotFoundException e) {
//            throw new RuntimeException(e);
//        }

        return new FeedforwardNeuronNetworkBuilderImpl();
    }

    @Nonnull
    @Override
    public BackPropagationLearnEngineBuilder createBackPropagationLearnEngineBuilder() {
        //todo
        throw new UnsupportedOperationException("Is not implemented yet.");
    }

    @Nonnull
    @Override
    public ResilientBackPropagationLearnEngineBuilder createResilientBackPropagationLearnEngineBuilder() {
        //todo
        throw new UnsupportedOperationException("Is not implemented yet.");
    }

    @Override
    @Nonnull
    public NeuronNetworkInputBuilder createInputBuilder() {
        return new NeuronNetworkInputBuilderImpl();
    }

    @Nonnull
    @Override
    public NeuronNetworkDataSetBuilder createTrainSetBuilder() {
        return new NeuronNetworkTrainSetBuilderImpl();
    }

    @Nonnull
    @Override
    public NeuronNetworkOutputBuilder createOutputBuilder() {
        return new NeuronNetworkOutputBuilderImpl();
    }
}
