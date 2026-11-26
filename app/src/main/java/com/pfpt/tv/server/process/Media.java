package com.pfpt.tv.server.process;

import com.pfpt.tv.player.PlaybackSnapshot;
import com.pfpt.tv.server.Nano;
import com.pfpt.tv.server.Server;
import com.pfpt.tv.server.impl.Process;
import com.pfpt.tv.service.PlaybackService;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public class Media implements Process {

    @Override
    public boolean isRequest(IHTTPSession session, String url) {
        return url.startsWith("/media");
    }

    @Override
    public Response doResponse(IHTTPSession session, String url, Map<String, String> files) {
        PlaybackService service = Server.get().getService();
        if (service == null) return Nano.ok("{}");
        return Nano.ok(PlaybackSnapshot.json());
    }
}
