package com.cherkovskiy.neuron_networks;

import com.cherkovskiy.application_context.ApplicationContextHolder;
import com.cherkovskiy.application_context.api.ServiceLifecycle;
import com.cherkovskiy.application_context.api.annotations.Service;
import com.cherkovskiy.application_context.api.annotations.ServiceInject;
import com.cherkovskiy.application_context.api.configuration.ConfigurationInject;
import com.cherkovskiy.neuron_networks.api.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;


@Service
public class NNExample implements ServiceLifecycle {

    private final NeuronNetworkService neuronNetworkService;
    private final Configuration configurationExample;

    public NNExample(@ServiceInject NeuronNetworkService neuronNetworkService) {
        this.neuronNetworkService = neuronNetworkService;
        this.configurationExample = ApplicationContextHolder.currentContext().getConfigurationContext().getOrResolve(Configuration.class);
    }

    //or
    public NNExample(@ServiceInject NeuronNetworkService neuronNetworkService,
                     @ConfigurationInject Configuration configurationExample) {
        this.neuronNetworkService = neuronNetworkService;
        this.configurationExample = configurationExample;
    }

    @Override
    public void postConstruct() throws Exception {
        //1. Create empty (random initialised) NN
        final NeuronNetwork neuronNetwork = neuronNetworkService.createFeedforwardBuilder()

                //.setActivationFunction(StandardActivationFunctions.HYPERBOLIC_TANG_ANGUITA) // работает действительно очень круто и быстро приходит к цели

                .setActivationFunction(BasicActivationFunction.HYPERBOLIC_TANG_ANGUITA)

                .inputsNeurons(3)
                .addHiddenLevel(5)
                .outputNeurons(1)
                .useBias(false)
                .build();


        // 2. Create onlineLearn builder for NN
        final BackPropagationLearnEngine backPropagationLearnEngine = neuronNetworkService.createBackPropagationLearnEngineBuilder()
                .learningRate(0.01)      //according to 92 page: 0.01 ≤ η ≤ 0.9
                .weightDecay(0.00001)    //try to avoid large weights //5.6.4 Weight decay: Punishment of large weights - improve generalization and reduce memorization.

                .setSuccessErrorValue(0.001)
                .setStopCondition(10000, 0.001)
                .setMaxEpochPerCycle(2000000)


                //Think about necessity of this approach - it is not optimal and more time consuming.
                //Instead I propose to consider: dropout
                // http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.50.4167&rep=rep1&type=pdf
                // http://www.cs.toronto.edu/~hinton/absps/JMLRdropout.pdf
                // Also I can try to implement http://yann.lecun.com/exdb/publis/pdf/lecun-90b.pdf - Pruning and Optimal Brain Damage
                .expand(true)
                .setMinNeuronsPerLevel(3)
                .setMaxNeuronsPerLevel(3000)
                .setMinLevels(1)
                .setMaxLevels(10)

                .logErrorFunction(1000)
                .setDebugMode(DebugLevels.DEBUG_EVERY_100_000)
                .useStatModule(false, 1., 0.1)
                .build();

        //3. Data for learning
        final NeuronNetworkDataSetBuilder neuronNetworkTrainSetBuilder = neuronNetworkService.createTrainSetBuilder();
        final NeuronNetworkInputBuilder inputBuilder = neuronNetworkService.createInputBuilder();
        final NeuronNetworkOutputBuilder outputBuilder = neuronNetworkService.createOutputBuilder();

        final NeuronNetworkDataSet neuronNetworkTrainSet = neuronNetworkTrainSetBuilder

                //Отлично обучается
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(1d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(1d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d)).build())

                //Отлично обучается на на большой сети
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d, 0d, 0d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d, 0d, 1d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 2d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d, 1d, 0d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 3d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d, 1d, 1d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 4d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d, 0d, 0d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 5d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d, 0d, 1d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 6d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d, 1d, 0d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 7d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d, 1d, 1d)).build())


                // На такой архитектуре - регулярно сталкиваемся с локальным миниумом - решенеи - https://www.researchgate.net/publication/220237893_Avoiding_the_Local_Minima_Problem_in_Backpropagation_Algorithm_with_Modified_Error_Function
                //                .inputsNeurons(3)
                //                .addHiddenLevel(4)
                //                .outputNeurons(1)
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 0d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 0d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 1d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 1d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(1d, 0d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(1d, 0d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(1d, 1d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d)).build())
//                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(1d, 1d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d)).build())


                //TODO: програмно создать сет для 64 бит например
                //set train data
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 0d, 0d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 0d, 0d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 0d, 1d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 0d, 1d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 1d, 0d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 1d, 0d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 1d, 1d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(0d, 1d, 1d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(1d, 0d, 0d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(1d, 0d, 0d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(1d, 0d, 1d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(1d, 0d, 1d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(1d, 1d, 0d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(1d, 1d, 0d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(1d, 1d, 1d, 0d)).build(), outputBuilder.setOutputValues(Arrays.asList(1d)).build())
                .setTrainInputAndOutput(inputBuilder.setInputValues(Arrays.asList(1d, 1d, 1d, 1d)).build(), outputBuilder.setOutputValues(Arrays.asList(0d)).build())

                .useToVerify(0.01f)

                .build();


        //3.1 Or load from file.
        final NeuronNetworkDataSet neuronNetworkTrainSetFromFile;
        try (InputStream inputStream = Files.newInputStream(Paths.get(configurationExample.getLogDir().getAbsolutePath(), "trainData.td"))) {
            neuronNetworkTrainSetFromFile = neuronNetworkTrainSetBuilder.useToVerify(0.1f).build(inputStream);
        }


        //4. Learn process
        final BackPropagationLearnResult backPropagationLearnResult = backPropagationLearnEngine.onlineLearn(neuronNetwork, neuronNetworkTrainSet);
        System.out.println(backPropagationLearnResult);

        //5. Serialize new NN
        try (OutputStream outputStream = Files.newOutputStream(Paths.get("neuronNetwork.nn"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            backPropagationLearnResult.getResultForTrainSet().getTopologyBestError().writeTo(outputStream);
        }

        //------------- Extra abilities ---------------------
        try (InputStream inputStream = Files.newInputStream(Paths.get("neuronNetwork.nn"))) {
            final NeuronNetwork neuronNetworkFromFile = neuronNetworkService.createFeedforwardBuilder().build(inputStream);
            final BackPropagationLearnResult estimation = backPropagationLearnEngine.estimate(neuronNetworkFromFile, neuronNetworkTrainSet);

            System.out.println(estimation.getResultForTrainSet().getBestError());
            estimation.getResultForVerifyingSet().ifPresent(r -> System.out.println(r.getBestError()));
        }
    }
}

