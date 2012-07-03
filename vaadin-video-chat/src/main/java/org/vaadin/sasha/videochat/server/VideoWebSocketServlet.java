package org.vaadin.sasha.videochat.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketFactory;

@SuppressWarnings("serial")
public class VideoWebSocketServlet extends HttpServlet implements WebSocketFactory.Acceptor {

    private Map<Integer, VideoChatSocket> idToSocket = new HashMap<Integer, VideoChatSocket>();
    
    WebSocketFactory _webSocketFactory;
    
    @Override
    public void init() throws ServletException {
        super.init();
        String bs=getInitParameter("bufferSize");
        _webSocketFactory = new WebSocketFactory(this,bs==null?8192:Integer.parseInt(bs));
        String max=getInitParameter("maxIdleTime");
        if (max!=null)
            _webSocketFactory.setMaxIdleTime(Integer.parseInt(max));
        
        max=getInitParameter("maxTextMessageSize");
        if (max!=null)
            _webSocketFactory.setMaxTextMessageSize(Integer.parseInt(max));
        
        max=getInitParameter("maxBinaryMessageSize");
        if (max!=null)
            _webSocketFactory.setMaxBinaryMessageSize(Integer.parseInt(max));
    }
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (_webSocketFactory.acceptWebSocket(request,response) || response.isCommitted())
            return;
        super.service(request, response);
    }
    
    
    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String ignore) {
        try {
            //logger.log(Level.INFO, "Creating websocket for " + request.getRequestURI());
            final String uri = request.getRequestURI();
            final Integer userId = Integer.parseInt(uri.substring(uri.lastIndexOf('/') + 1));
            final VideoChatSocket ws = new VideoChatSocket(userId);
            idToSocket.put(userId, ws);
            return ws;            
        } catch (NumberFormatException ex) {
            //logger.log(Level.SEVERE, ex.getMessage());
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getServletContext().getNamedDispatcher("default").forward(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getServletContext().getNamedDispatcher("default").forward(req, resp);
    }
    
    
    public class VideoChatSocket implements WebSocket.OnTextMessage {

        private int userId;
        
        private Connection connection;
        
        public VideoChatSocket(int userId) {
            super();
            this.userId = userId;
        }
        
        @Override
        public void onClose(int arg0, String arg1) {
            for (final Integer id : idToSocket.keySet()) {
                if (idToSocket.get(id) == this) {
                    idToSocket.remove(id);
                }
            }
        }

        @Override
        public void onOpen(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void onMessage(String jsonMessage) {
            System.out.println(jsonMessage);
            final Iterator<Map.Entry<Integer, VideoChatSocket>> it = idToSocket.entrySet().iterator();
            while (it.hasNext()) {
                final Entry<Integer, VideoChatSocket> entry = it.next();
                if (userId != entry.getKey()) {
                    try {
                        entry.getValue().connection.sendMessage(jsonMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    @Override
    public boolean checkOrigin(HttpServletRequest request, String origin) {
        return true;
    }
    

}