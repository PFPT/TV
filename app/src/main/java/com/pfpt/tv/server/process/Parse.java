package com.pfpt.tv.server.process;

import static fi.iki.elonen.NanoHTTPD.MIME_HTML;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import com.pfpt.tv.server.Nano;
import com.pfpt.tv.server.impl.Process;
import com.pfpt.tv.utils.StoragePaths;
import com.github.catvod.utils.Asset;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class Parse implements Process {

    @Override
    public boolean isRequest(IHTTPSession session, String url) {
        return url.startsWith("/parse");
    }

    @Override
    public Response doResponse(IHTTPSession session, String url, Map<String, String> files) {
        try {
            Map<String, String> params = session.getParms();
            String html = Asset.read("parse.html").replace("__JXS__", StoragePaths.js(params.get("jxs"))).replace("__URL__", StoragePaths.js(params.get("url")));
            return newFixedLengthResponse(Status.OK, MIME_HTML, html);
        } catch (Exception e) {
            return Nano.error(e.getMessage());
        }
    }
}
