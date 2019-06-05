package heronarts.lx.mutation;

import heronarts.lx.LX;
import heronarts.lx.data.ProjectData;
import heronarts.lx.data.ProjectLoadResponse;
import heronarts.lx.data.ProjectLoaderGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class LXMutationServer {
    public static final int PORT = 3031;
    private final LX lx;
    private final Server server;

    public LXMutationServer(LX lx) {
        this.lx = lx;
        server = ServerBuilder.forPort(PORT)
            .addService(new MutationServiceImpl(lx))
            .addService(new ProjectServiceImpl(lx))
            .build();
    }

    public void start() throws IOException {
        server.start();
        System.out.println(String.format("LXMutationServer started, listening on " + PORT));
    }

    public void dispose() {
        server.shutdown();
    }

    private static class MutationServiceImpl extends MutationServiceGrpc.MutationServiceImplBase {
        private final LX lx;

        MutationServiceImpl(LX lx) {
            this.lx = lx;
        }

        @Override
        public void apply(Mutation mut, StreamObserver<MutationResult> response) {
            System.out.println("received " + mut);
            lx.engine.mutations.enqueue(new LXMutationQueue.MutationRequest(mut, e -> {
                if (e == null) {
                    response.onNext(MutationResult.newBuilder().build());
                    response.onCompleted();
                } else {
                    response.onError(e);
                }
            }));
        }
    }

    private static class ProjectServiceImpl extends ProjectLoaderGrpc.ProjectLoaderImplBase {
        private final LX lx;

        ProjectServiceImpl(LX lx) {
            this.lx = lx;
        }

        @Override
        public void load(ProjectData pd, StreamObserver<ProjectLoadResponse> response) {

        }
    }
}
