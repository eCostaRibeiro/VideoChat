package org.vaadin.sasha.videochat.client.ui.login;

import javax.inject.Inject;

import org.vaadin.sasha.videochat.client.SessionInfo;
import org.vaadin.sasha.videochat.client.VideoChatServiceAsync;
import org.vaadin.sasha.videochat.client.event.UserLogedInEvent;
import org.vaadin.sasha.videochat.client.ui.chat.VideoChatPlace;
import org.vaadin.sasha.videochat.client.widget.dialog.confirmation.NotificationDialog;
import org.vaadin.sasha.videochat.shared.domain.User;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

public class LoginActivity extends AbstractActivity implements LoginView.Presenter {

    private final VideoChatServiceAsync service;
    
    private final LoginView view;
    
    private final User user = new User();
    
    private boolean isAuthenticated = false;

    private SessionInfo sessionInfo;
    
    private PlaceController controller;
    
    private EventBus eventBus;
    
    @Inject
    public LoginActivity(final LoginView view, VideoChatServiceAsync service, 
            PlaceController controller, SessionInfo sessionInfo, EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        this.controller = controller;
        this.sessionInfo = sessionInfo;
        this.service = service;
        this.view = view;
        view.setPresenter(this);
    }
    
    @Override
    public String mayStop() {
        final String msg = isAuthenticated ? super.mayStop() : "Please authenticate";
        return msg;
    }
    
    @Override
    public void start(AcceptsOneWidget viewport, com.google.gwt.event.shared.EventBus eventBus) {
        viewport.setWidget(view.asWidget());
    }

    @Override
    public void signIn() {
        service.signIn(user, new AsyncCallback<Integer>() {
            
            @Override
            public void onSuccess(Integer userId) {
                onLoginSuccess(userId);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                (new NotificationDialog("Login Failed")).show();
            }
        });
    }
   
    @Override
    public void register() {
        service.register(user, new AsyncCallback<Integer>() {
            
            @Override
            public void onSuccess(Integer userId) {
                onLoginSuccess(userId);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                (new NotificationDialog("Login Failed")).show();
            }
        });
    }
    
    @Override
    public void setUserName(String userName) {
        user.setUserName(userName);
    }

    @Override
    public void setEmail(String email) {
        user.setEmail(email);
    }
    
    private void onLoginSuccess(Integer userId) {
        isAuthenticated = true;
        sessionInfo.setUserId(userId);
        controller.goTo(new VideoChatPlace());
        (new NotificationDialog("Login sucess")).show();
        eventBus.fireEvent(new UserLogedInEvent(userId));
    }
    
}
