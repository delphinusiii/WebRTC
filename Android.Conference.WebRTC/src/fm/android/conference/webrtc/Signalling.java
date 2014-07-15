package fm.android.conference.webrtc;

import android.util.Log;
import fm.*;
import fm.icelink.*;
import fm.icelink.websync.*;
import fm.websync.*;
import fm.websync.subscribers.*;

// Peers have to exchange information when setting up P2P links,
// like descriptions of the streams (called the offer or answer)
// and information about network addresses (called candidates).
// IceLink generates this information for you automatically.
// Your responsibility is to pass messages back and forth between
// peers as quickly as possible. This is called "signalling".
public class Signalling
{
    // We're going to use WebSync for this example, but any real-time
    // messaging system will do (like SIP or XMPP). We use WebSync
    // since it works well with JavaScript and uses HTTP, which is
    // widely allowed. To use something else, simply replace the calls
    // to WebSync with calls to your library.
    private String  websyncRequestUrl = "http://67.205.102.175:8080/websync.ashx"; // 10.0.2.2 is the Android emulator host
    private boolean websyncExtension  = true;
    private Client  websyncClient     = null;
    
    public Signalling()
    {
        try
        {
            // Create a WebSync client.
            websyncClient = new Client(websyncRequestUrl);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    private Exception lastStartException = null;
    public Exception getLastStartException()
    {
        return lastStartException;
    }
    
    public void start(final SingleAction<Exception> callback)
    {
        try
        {
            // Create a persistent connection to the server.
            websyncClient.connect(new ConnectArgs()
            {{
                setOnSuccess(new SingleAction<ConnectSuccessArgs>()
                {
                    public void invoke(ConnectSuccessArgs e)
                    {
                        callback.invoke(null);
                    }
                });
                setOnFailure(new SingleAction<ConnectFailureArgs>()
                {
                    public void invoke(ConnectFailureArgs e)
                    {
                        e.setRetry(false);
                        lastStartException = e.getException();
                        callback.invoke(e.getException());
                    }
                });
            }});
        }
        catch (Exception ex)
        {
            lastStartException = ex;
            callback.invoke(ex);
        }
    }
    
    private Exception lastStopException = null;
    public Exception getLastStopException()
    {
        return lastStopException;
    }
    
    public void stop(final SingleAction<Exception> callback)
    {
        try
        {
            // Tear down the persistent connection.
            websyncClient.disconnect(new DisconnectArgs()
            {{
                setOnComplete(new SingleAction<DisconnectCompleteArgs>()
                {
                    public void invoke(DisconnectCompleteArgs e)
                    {
                        lastStopException = e.getException();
                        callback.invoke(e.getException());
                    }
                });
            }});
        }
        catch (Exception ex)
        {
            lastStopException = ex;
            callback.invoke(ex);
        }
    }
    
    private Exception lastAttachException = null;
    public Exception getLastAttachException()
    {
        return lastAttachException;
    }
    
    public void attach(final Conference conference, final SingleAction<Exception> callback)
    {
        try
        {
            // IceLink includes a WebSync client extension that will
            // automatically manage session negotiation for you. If
            // you are not using WebSync, see the 'else' block for a
            // session negotiation template.
            if (websyncExtension)
            {
                // Manage the conference automatically using a WebSync
                // channel. P2P links will be created automatically to
                // peers that join the same channel.
                ClientExtensions.joinConference(websyncClient, new JoinConferenceArgs("/myvideochat", conference)
                {{
                    setOnSuccess(new SingleAction<JoinConferenceSuccessArgs>()
                    {
                        public void invoke(JoinConferenceSuccessArgs e)
                        {
                            callback.invoke(null);
                        }
                        
                    });
                    setOnFailure(new SingleAction<JoinConferenceFailureArgs>()
                    {
                        public void invoke(JoinConferenceFailureArgs e)
                        {
                            lastAttachException = e.getException();
                            callback.invoke(e.getException());
                        }
                    });
                }});
            }
            else
            {
                // If the WebSync stream goes down, destroy all P2P links.
                // The WebSync client reconnect procedure will cause new
                // P2P links to be created.
                websyncClient.addOnStreamFailure(new SingleAction<StreamFailureArgs>()
                {
                    public void invoke(StreamFailureArgs e)
                    {
                        try
                        {
                            conference.unlinkAll();
                        }
                        catch (Exception ex)
                        { ex.printStackTrace(); }
                    }
                });
    
                // Add a couple event handlers to the conference to send
                // generated offers/answers and candidates to a peer.
                // The peer ID is something we define later. In this case,
                // it represents the remote WebSync client ID. WebSync's
                // "notify" method is used to send data to a specific client.
                conference.addOnLinkOfferAnswer(new SingleAction<LinkOfferAnswerArgs>()
                {
                    public void invoke(LinkOfferAnswerArgs e)
                    {
                        try
                        {
                            websyncClient.notify(new NotifyArgs(new Guid(e.getPeerId()), e.getOfferAnswer().toJson(), "offeranswer"));
                            
                        }
                        catch (Exception ex)
                        { ex.printStackTrace(); }
                    }
                });
                conference.addOnLinkCandidate(new SingleAction<LinkCandidateArgs>()
                {
                    public void invoke(LinkCandidateArgs e)
                    {
                        try
                        {
                            websyncClient.notify(new NotifyArgs(new Guid(e.getPeerId()), e.getCandidate().toJson(), "candidate"));
                        }
                        catch (Exception ex)
                        { ex.printStackTrace(); }
                    }
                });
    
                // Add an event handler to the WebSync client to receive
                // incoming offers/answers and candidates from a peer.
                // Call the "receiveOfferAnswer" or "receiveCandidate"
                // method to pass the information to the conference.
                websyncClient.addOnNotify(new SingleAction<NotifyReceiveArgs>()
                {
                    public void invoke(NotifyReceiveArgs e)
                    {
                        try
                        {
                            String peerId = e.getNotifyingClient().getClientId().toString();
                            Object peerState = e.getNotifyingClient().getBoundRecords();
                            
                            if (e.getTag().equals("offeranswer"))
                            {
                                conference.receiveOfferAnswer(OfferAnswer.fromJson(e.getDataJson()), peerId, peerState);
                               
                            }
                            else if (e.getTag().equals("candidate"))
                            {
                                conference.receiveCandidate(Candidate.fromJson(e.getDataJson()), peerId);
                            }
                        }
                        catch (Exception ex)
                        { ex.printStackTrace(); }
                    }
                });
    
                // Subscribe to a WebSync channel. When another client joins the same
                // channel, create a P2P link. When a client leaves, destroy it.
                SubscribeArgs subscribeArgs = new SubscribeArgs("/myvideochat")
                {{
                    setOnSuccess(new SingleAction<SubscribeSuccessArgs>()
                    {
                        public void invoke(SubscribeSuccessArgs e)
                        {
                            callback.invoke(null);
                        }
                    });
                    setOnFailure(new SingleAction<SubscribeFailureArgs>()
                    {
                        public void invoke(SubscribeFailureArgs e)
                        {
                            lastAttachException = e.getException();
                            callback.invoke(e.getException());
                        }
                    });
                    setOnReceive(new SingleAction<SubscribeReceiveArgs>()
                    {
                        public void invoke(SubscribeReceiveArgs e) { }
                    });
                }};
                SubscribeArgsExtensions.setOnClientSubscribe(subscribeArgs, new SingleAction<ClientSubscribeArgs>()
                {
                    public void invoke(ClientSubscribeArgs e)
                    {
                        try
                        {
                            String peerId = e.getSubscribedClient().getClientId().toString();
                            Object peerState = e.getSubscribedClient().getBoundRecords();
                            conference.link(peerId, peerState);
                        }
                        catch (Exception ex)
                        { ex.printStackTrace(); }
                    }
                });
                SubscribeArgsExtensions.setOnClientUnsubscribe(subscribeArgs, new SingleAction<ClientUnsubscribeArgs>()
                {
                    public void invoke(ClientUnsubscribeArgs e)
                    {
                        try
                        {
                            String peerId = e.getUnsubscribedClient().getClientId().toString();
                            conference.unlink(peerId);
                        }
                        catch (Exception ex)
                        { ex.printStackTrace(); }
                    }
                });
                websyncClient.subscribe(subscribeArgs);
            }
        }
        catch (Exception ex)
        {
            lastAttachException = ex;
            callback.invoke(ex);
        }
    }
}
