// ------------------------------------------------------------
//  Copyright (c) Microsoft Corporation.  All rights reserved.
//  Licensed under the MIT License (MIT). See License.txt in the repo root for license information.
// ------------------------------------------------------------

package counteractor;

import counterinterface.CounterActor;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import microsoft.servicefabric.actors.runtime.ActorServiceAttribute;
import microsoft.servicefabric.actors.runtime.FabricActor;
import microsoft.servicefabric.actors.runtime.StatePersistence;
import microsoft.servicefabric.actors.runtime.StatePersistenceAttribute;
import microsoft.servicefabric.actors.ActorId;
import microsoft.servicefabric.actors.runtime.FabricActorService;

/**
StatePersistenceAttribute can be of 3 types - persisted, volatile, null. 
We can use based on our requirement.
*/

@ActorServiceAttribute(name = "CounterActorService")
@StatePersistenceAttribute(statePersistence = StatePersistence.Persisted) 
public class CounterActorImpl extends FabricActor implements CounterActor {
    public CounterActorImpl(FabricActorService actorService, ActorId actorId) {
        super(actorService, actorId);
    }

    @Override
    protected CompletableFuture<?> onActivateAsync() {
        this.registerTimer((o) -> this.updateCounter(o), "updateCounter", this, Duration.ofMillis(1000), Duration.ofMillis(1000));
        return this.stateManager().tryAddStateAsync("count", 0);
    }

    @Override
    public CompletableFuture<Integer> getCountAsync() {
        return this.stateManager().getStateAsync("count");
    }

    @Override
    public CompletableFuture<?> setCountAsync(int count) {
        return this.stateManager().addOrUpdateStateAsync("count", count, (key, value) -> count > value ? count : value);
    }

    private CompletableFuture<?> updateCounter(Object state) {
        return this.stateManager().addOrUpdateStateAsync("count", 0, (key, value) -> value + 1);
    }
}
