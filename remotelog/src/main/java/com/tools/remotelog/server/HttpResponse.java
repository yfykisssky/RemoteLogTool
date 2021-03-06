package com.tools.remotelog.server;

import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpResponse {

    private static final String REQUEST_ACTION = "action";
    private static List<ResponseHandle> sResponseHandles;

    public static void handle(Socket socket, Map<String, String> headerMap) throws IOException {
        if (sResponseHandles == null) {
            synchronized (HttpResponse.class) {
                sResponseHandles = new ArrayList<ResponseHandle>() {{
                    add(new ListFileResponseHandle());
                    add(new DownloadFileResponseHandle());
                }};
            }
        }

        String getUrl = headerMap.get(WebSocket.REQUEST_LINE);
        //只处理了基本的请求
        int start = getUrl.indexOf('?');
        int end = getUrl.lastIndexOf("HTTP/1.1");
        if (start != -1 && end != -1) {
            Map<String, String> queryString = EncodeUtils.parseUrlQueryString(getUrl.substring(start + 1, end));
            String action = queryString.get(REQUEST_ACTION);
            if (action != null) {
                for (ResponseHandle handle : sResponseHandles) {
                    if (handle.isMatchAction(action)) {
                        handle.hanlde(queryString, socket.getOutputStream());
                        break;
                    }
                }
            }
        }

    }


    //error response
    private static void responseInnerError(OutputStream outputStream, int code, String msg, String content, Throwable e) throws IOException {
        StringBuilder sb = new StringBuilder("HTTP/1.1 ").append(code).append(msg).append("\r\n");
        sb.append("\r\n\r\n");
        if (content != null) {
            sb.append(content).append("\r\n");
        }
        if (e != null) {
            sb.append(e.getMessage());
        }
        outputStream.write(sb.toString().getBytes());
        outputStream.flush();
    }

    private interface ResponseHandle {
        boolean isMatchAction(String action);

        void hanlde(Map<String, String> query, OutputStream outputStream) throws IOException;
    }

    private static abstract class AbsResponseHandle implements ResponseHandle {

        void writeHeaders(OutputStream outputStream, Map<String, String> headers) throws IOException {
            StringBuilder sb = new StringBuilder("HTTP/1.1 200 OK \r\n");

            if (headers != null) {
                Set<Map.Entry<String, String>> entries = headers.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
                }
                sb.append("\r\n");
            } else {
                sb.append("\r\n\r\n");
            }
            outputStream.write(sb.toString().getBytes());
        }

        boolean formatJsonp(Map<String, String> query){
            return  "jsonp".equals(query.get("format"))&&query.containsKey("callback");
        }
    }


    private static class ListFileResponseHandle extends AbsResponseHandle {

        static final File DEFAULT_DIR=Environment.getExternalStorageDirectory();

        @Override
        public boolean isMatchAction(String action) {
            return "list".equals(action);
        }

        @Override
        public void hanlde(Map<String, String> query, OutputStream outputStream) throws IOException {
            String dir = query.get("dir");
            File file = (dir == null ? DEFAULT_DIR : new File(dir));

            if (!file.exists()) {
                file = DEFAULT_DIR;
            }

            String[] list = file.list();
            String output="";
            try {
                if(list != null) {
                    ArrayList<String> dirs=new ArrayList<String>();
                    ArrayList<String> files=new ArrayList<String>();
                    for (String path:list){
                        File file1=new File(file.getAbsolutePath(),path);
                        if(file1.isDirectory()){
                            dirs.add(file1.getName());
                        }else if(file1.isFile()){
                            files.add(file1.getName());
                        }
                    }

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("dir", file.getAbsolutePath());

                    jsonObject.put("file_list", new JSONArray(files));
                    jsonObject.put("dir_list",new JSONArray(dirs));

                    if (formatJsonp(query)) {
                        output = query.get("callback") + "(" + jsonObject.toString() + ")";
                    } else {
                        output = jsonObject.toString();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] data = output.getBytes();

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            headers.put("Content-Length", "" + data.length);

            writeHeaders(outputStream, headers);
            outputStream.write(data);
            outputStream.flush();

        }
    }

    private static class DownloadFileResponseHandle extends AbsResponseHandle {

        @Override
        public boolean isMatchAction(String action) {
            return "download".equals(action);
        }

        @Override
        public void hanlde(Map<String, String> query, OutputStream outputStream) throws IOException {
            String path = query.get("path");
            if (path == null) {
                path = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                respFile(file, outputStream);
            } else {
                responseInnerError(outputStream, 404, "Not Found", "file " + path + " not found !", null);
            }
        }

        private void respFile(File file, OutputStream outputStream) throws IOException {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/octet-stream");
            headers.put("Content-Length", "" + file.length());
            headers.put("Content-Disposition", "attachment;filename=\"" + file.getName() + "\"");
            writeHeaders(outputStream, headers);

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                byte[] buff = new byte[8192];
                int len = -1;
                while ((len = fis.read(buff)) != -1) {
                    outputStream.write(buff, 0, len);
                }
                outputStream.flush();
            }  catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
