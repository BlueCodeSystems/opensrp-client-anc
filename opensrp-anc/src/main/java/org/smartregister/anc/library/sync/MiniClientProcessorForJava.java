package org.smartregister.anc.library.sync;

import android.support.annotation.NonNull;

import org.smartregister.domain.db.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.jsonmapping.ClientClassification;

import java.util.HashSet;
import java.util.List;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-07-31
 */

public interface MiniClientProcessorForJava {

    @NonNull
    HashSet<String> getEventType();

    boolean canProcess(@NonNull String eventType);

    void processEventClient(@NonNull EventClient eventClient, @NonNull List<Event> unsyncEvents, @NonNull ClientClassification clientClassification) throws Exception;
}
