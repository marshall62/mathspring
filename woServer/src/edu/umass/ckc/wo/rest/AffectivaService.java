package edu.umass.ckc.wo.rest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
//import javax.ws.rs.*;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 10/5/2016.
 */

@Path("/affectiva")
public class AffectivaService {

    private final static Logger logger = Logger.getLogger(AffectivaService.class);

    @javax.ws.rs.core.Context ServletContext servletContext;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/{sessionId}")
    public Response postFacialData (String postdata, @PathParam("sessionId") String sessId) throws Exception {
        JSONTokener tokener = new JSONTokener(postdata); //uri.toURL().openStream());
        JSONObject root = new JSONObject(tokener);
        final JSONArray emotions = root.getJSONArray("emotions");
        final JSONArray facepoints = root.getJSONArray(("facepoints"));
        for (int i = 0; i < emotions.length(); i++) {
            JSONObject em = emotions.getJSONObject(i);
            AffectivaEmotion ae = AffectivaEmotion.createFromJSON(em);
            System.out.println(ae.toString());
        }
        for (int i = 0; i < facepoints.length(); i++) {
            JSONObject fp = facepoints.getJSONObject(i);
            AffectivaFacePoint afp = AffectivaFacePoint.createFromJSON(fp);
            System.out.println(afp.toString());
        }

//        List<AffectivaFacePoint> points = aReq.getFaceDataPoints();
        root.put("sessionId",sessId);
        JSONObject o = new JSONObject();
        root.put("message","success");
        return Response.status(200).entity(root.toString()).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/test/{sessionId}")
    public Response postFacialData (@PathParam("sessionId") String sessId) throws Exception {
        List<AffectivaEmotion> emotions = new ArrayList<AffectivaEmotion>();
        AffectivaEmotion ee = new AffectivaEmotion("joy",30.3);
        emotions.add(ee);
        ee = new AffectivaEmotion("disgust",4.56);
        emotions.add(ee);
//        List<AffectivaFacePoint> points = aReq.getFaceDataPoints();
        logger.debug("sessionId: " + sessId);
//        for (AffectivaFacePoint p : points)  {
//            logger.debug("Point: " + p);
//
//        }
        for (AffectivaEmotion e : emotions) {
            logger.debug("Emotion: " + e);
        }
        JSONObject o = new JSONObject();
        o.put("message","success");
        return Response.status(200).entity(o.toString()).build();
    }


    // http://localhost:8080/mt/affectiva/userEmotion/1234
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/userEmotion/{sessionId}")
    public Response getUserEmotion (@PathParam("sessionId") String sessId) throws Exception {
        logger.debug("request for " + sessId);
        AffectivaEmotion em = new AffectivaEmotion("disgust",0.56);
        JSONObject o = new JSONObject();
        o.put("hi","there");
        return Response.status(200).entity(o.toString()).build();
//        return em;
    }


    @POST
    @Path("/simpost")
    @Consumes("application/json")
    public Response crunchifyREST(InputStream incomingData) {
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
        } catch (Exception e) {
            System.out.println("Error Parsing: - ");
        }
        System.out.println("Data Received: " + crunchifyBuilder.toString());

        // return HTTP response 200 in case of success
        return Response.status(200).entity(crunchifyBuilder.toString()).build();
    }

}
