package io.cybermiles.abci.travis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jtendermint.jabci.api.ICheckTx;
import com.github.jtendermint.jabci.api.ICommit;
import com.github.jtendermint.jabci.api.IDeliverTx;
import com.github.jtendermint.jabci.api.IQuery;
import com.github.jtendermint.jabci.socket.TSocket;
import com.github.jtendermint.jabci.types.Types.*;
import com.github.jtendermint.jabci.CodeType;
import com.google.protobuf.ByteString;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Set;

public final class TravisApp implements IDeliverTx, ICheckTx, ICommit, IQuery {

    private TSocket socket;

    public static void main(String[] args) throws Exception {
        new TravisApp ();
    }

    public TravisApp () throws InterruptedException {
        System.out.println("Starting Travis ABCI");
        socket = new TSocket();
        socket.registerListener(this);

        // Do NOT subclass
        Thread t = new Thread(socket::start);
        t.setName("Travis ABCI Thread");
        t.start();
        while (true) {
            Thread.sleep(1000L);
        }
    }

    @Override
    public ResponseCheckTx requestCheckTx (RequestCheckTx req) {
        ByteString tx = req.getTx();
        System.out.println("CheckTx: " + tx.toStringUtf8());

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://127.0.0.1:8088/check_tx");
        httppost.setEntity(new ByteArrayEntity(tx.toByteArray()));
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            String s = EntityUtils.toString(entity);
            System.out.println(s);
            EntityUtils.consume(entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseCheckTx.newBuilder().setCode(CodeType.OK_VALUE).build();
    }

    @Override
    public ResponseDeliverTx receivedDeliverTx (RequestDeliverTx req) {
        ByteString tx = req.getTx();
        System.out.println("DeliverTx: " + tx.toStringUtf8());

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://127.0.0.1:8088/deliver_tx");
        httppost.setEntity(new ByteArrayEntity(tx.toByteArray()));
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            String s = EntityUtils.toString(entity);
            System.out.println(s);
            EntityUtils.consume(entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseDeliverTx.newBuilder().setCode(CodeType.OK_VALUE).build();
    }

    @Override
    public ResponseCommit requestCommit (RequestCommit req) {
        System.out.println("Commit");
        ResponseCommit resp = ResponseCommit.newBuilder().setCode(CodeType.OK_VALUE).build();
//        ResponseCommit resp = ResponseCommit.newBuilder().setCode(CodeType.InternalError_VALUE).build();
//
//        ByteArrayOutputStream bo = new ByteArrayOutputStream();
//
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        HttpPost httppost = new HttpPost("http://127.0.0.1:8088/commit");
//        CloseableHttpResponse response = null;
//        try {
//            req.writeTo(bo);
//            httppost.setEntity(new ByteArrayEntity(bo.toByteArray()));
//
//            response = httpclient.execute(httppost);
//            System.out.println(response.getStatusLine());
//            HttpEntity entity = response.getEntity();
//            byte [] b = EntityUtils.toByteArray(entity);
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode node = mapper.readTree(b);
//            String str = node.asText();
//            System.out.println(str);
//            EntityUtils.consume(entity);
//
//            ResponseCommit.Builder respBuilder = ResponseCommit.newBuilder();
//            respBuilder.mergeFrom(Base64.getDecoder().decode(str));
//            resp = respBuilder.build();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                response.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        return resp;
    }

    @Override
    public ResponseQuery requestQuery (RequestQuery req) {
        System.out.println("Query");
        
        ResponseQuery resp = ResponseQuery.newBuilder().setCode(CodeType.InternalError_VALUE).build();
        ByteArrayOutputStream bo = new ByteArrayOutputStream();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://127.0.0.1:8088/query");
        CloseableHttpResponse response = null;
        try {
            req.writeTo(bo);
            httppost.setEntity(new ByteArrayEntity(bo.toByteArray()));

            response = httpclient.execute(httppost);
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            byte [] b = EntityUtils.toByteArray(entity);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(b);
            String str = node.asText();
            System.out.println(str);
            EntityUtils.consume(entity);

            ResponseQuery.Builder respBuilder = ResponseQuery.newBuilder();
            respBuilder.mergeFrom(Base64.getDecoder().decode(str));
            resp = respBuilder.build();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resp;
    }
}
