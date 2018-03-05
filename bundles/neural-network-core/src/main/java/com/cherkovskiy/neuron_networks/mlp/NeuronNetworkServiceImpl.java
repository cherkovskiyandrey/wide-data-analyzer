package com.cherkovskiy.neuron_networks.mlp;

import com.cherkovskiy.application_context.api.annotations.Service;
import com.cherkovskiy.application_context.api.annotations.ServiceInject;
import com.cherkovskiy.application_context.api.annotations.ServiceVersion;
import com.cherkovskiy.comprehensive_serializer.api.SerializerService;
import com.cherkovskiy.neuron_networks.api.*;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


@Service(
        name = "mlp",
        type = Service.Type.SINGLETON,
        initType = Service.InitType.EAGER
)
public class NeuronNetworkServiceImpl implements NeuronNetworkService {

    //TODO: or me can use like this
    //private final ApplicationContext applicationContext;
//    public NeuronNetworkServiceImpl(ApplicationContext applicationContext) {
//        this.applicationContext = applicationContext;
//    }

    private final SerializerService serializerService;

    public NeuronNetworkServiceImpl(@ServiceInject SerializerService serializerService) {
        this.serializerService = serializerService;
    }

    @PostConstruct
    void init() {
    }

    @PreDestroy
    void destroy() {
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
