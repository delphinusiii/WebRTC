package fm.android.conference.webrtc;

import fm.*;
import fm.icelink.*;
import fm.icelink.webrtc.*;
import android.view.*;
import android.widget.Toast;
import avp8.*;

public class App {
	// Change the STUN/TURN server address
	// and port as needed for your environment.
	// Any STUN/TURN server will work. STUN is
	// required for WAN connections, and TURN
	// is required in some special cases. Peers
	// are not required to use the same server.
	private String icelinkServerAddress = "67.205.102.175"; // 10.0.2.2 is the
															// Android emulator
															// host
	private int icelinkServerPort = 3478; // 3478 is the default STUN/TURN port

	private Signalling signalling;
	private LocalMedia localMedia;
	private Conference conference;

	static {
		// Log to the console.
		Log.setProvider(new AndroidLogProvider(LogLevel.Info));

		try {
			// WebRTC has chosen VP8 as its mandatory video codec.
			// Since video encoding is best done using native code,
			// reference the video codec at the application-level.
			// This is required when using a WebRTC video stream.
			VideoStream.registerCodec("VP8", new EmptyFunction<VideoCodec>() {
				public VideoCodec invoke() {
					return new Vp8Codec();
				}
			}, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static App app;

	public static synchronized App getInstance() {
		if (app == null) {
			app = new App();
		}
		return app;
	}

	private App() {
	}

	public boolean signallingStarted() {
		return (signalling != null);
	}

	public void startSignalling(SingleAction<Exception> callback) {
		if (signalling == null) {
			// Create and start the signalling engine.
			signalling = new Signalling();
			signalling.start(callback);
		} else {
			callback.invoke(signalling.getLastStartException());
		}
	}
	public void stopSignalling(SingleAction<Exception> callback){
		if(signalling!=null)signalling.stop(callback);
		signalling=null;
	}
	public boolean localMediaStarted() {
		return (localMedia != null);
	}

	public void startLocalMedia(SingleAction<Exception> callback) {
		if (localMedia == null) {
			// Create and start the local media engine.
			localMedia = new LocalMedia();
			localMedia.start(callback);
		} else {
			callback.invoke(localMedia.getLastStartException());
		}
	}
	public void stopLocalMedia(SingleAction<Exception> callback){
		if(localMediaStarted())localMedia.stop(callback);
		localMedia = null;
		
	}

	public boolean conferenceStarted() {
		return (conference != null);
	}

	private Exception lastStartConferenceException = null;

	
	public void stopConference(){
		if(conferenceStarted())conference = null;
	}
	public void startConference(ViewGroup videoContainer,
			final SingleAction<Exception> callback) {
		if (!signallingStarted()) {
			callback.invoke(new Exception("Signalling must be started first."));
		} else if (!localMediaStarted()) {
			callback.invoke(new Exception("Local media must be started first."));
		} else if (conference != null) {
			callback.invoke(lastStartConferenceException);
		} else {
			try {
				// This is our local video control, a Java Component
				// or Android View. It is constantly updated with our
				// live video feed since we requested video above. Add
				// it directly to the UI or use the IceLink layout manager,
				// which we do below.
				Object localVideoControl = localMedia.getLocalVideoControl();

				// Create an IceLink layout manager, which makes the task
				// of arranging video controls easy. Give it a reference
				// to a Java Container that can be filled with video feeds.
				// For Android users, the WebRTC extension includes
				// AndroidLayoutManager, which accepts an Android ViewGroup.
				final AndroidLayoutManager layoutManager = new AndroidLayoutManager(
						videoContainer);

				// Position and display the local video control on-screen
				// by passing it to the layout manager created above.
				layoutManager.setLocalVideoControl(localVideoControl);

				// Create a WebRTC audio stream description (requires a
				// reference to the local audio feed).
				AudioStream audioStream = new AudioStream(
						localMedia.getLocalStream());

				// Create a WebRTC video stream description (requires a
				// reference to the local video feed). Whenever a P2P link
				// initializes using this description, position and display
				// the remote video control on-screen by passing it to the
				// layout manager created above. Whenever a P2P link goes
				// down, remove it.
				VideoStream videoStream = new VideoStream(
						localMedia.getLocalStream());
				videoStream
						.addOnLinkInit(new SingleAction<StreamLinkInitArgs>() {
							public void invoke(final StreamLinkInitArgs e) {
								Object remoteVideoControl = LinkExtensions
										.getRemoteVideoControl(e.getLink());
								try {
									layoutManager.addRemoteVideoControl(
											e.getPeerId(), remoteVideoControl);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						});
				videoStream
						.addOnLinkDown(new SingleAction<StreamLinkDownArgs>() {
							public void invoke(final StreamLinkDownArgs e) {
								layoutManager.removeRemoteVideoControl(e
										.getPeerId());
							}
						});

				// Create a conference using our stream descriptions.
				conference = new Conference(icelinkServerAddress,
						icelinkServerPort, new Stream[] { audioStream,
								videoStream });

				// Supply TURN relay credentials in case we are behind a
				// highly restrictive firewall. These credentials will be
				// verified by the TURN server.
				conference.setRelayUsername("test");
				conference.setRelayPassword("pa55w0rd!");

				// Add a few event handlers to the conference so we can see
				// when a new P2P link is created or changes state.
				conference.addOnLinkInit(new SingleAction<LinkInitArgs>() {
					public void invoke(LinkInitArgs e) {
						Log.info("Link to peer initializing...");
					}
				});
				conference.addOnLinkUp(new SingleAction<LinkUpArgs>() {
					public void invoke(LinkUpArgs e) {
						Log.info("Link to peer is UP.");
					}
				});
				conference.addOnLinkDown(new SingleAction<LinkDownArgs>() {
					public void invoke(LinkDownArgs e) {
						Log.info("Link to peer is DOWN. "
								+ e.getException().getMessage());
					}
				});

				signalling.attach(conference, new SingleAction<Exception>() {
					public void invoke(Exception ex) {
						if (ex != null) {
							ex = new Exception(
									"Could not attach signalling to conference.",
									ex);
							lastStartConferenceException = ex;
						}

						callback.invoke(ex);
					}
				});
				
				
			} catch (Exception ex) {
				lastStartConferenceException = ex;
				callback.invoke(ex);
			}
		}
	}
	public void getMediaToLayout(ViewGroup videoContainer,final SingleAction<Exception> callback) {
			try {
				Object localVideoControl = localMedia.getLocalVideoControl();
				final AndroidLayoutManager layoutManager = new AndroidLayoutManager(videoContainer);
				layoutManager.setLocalVideoControl(localVideoControl);

			} catch (Exception ex) {
				
				callback.invoke(ex);
			}
		}
	public void switchCamera() {
		if (localMedia != null) {
			localMedia.switchCamera();
		}
	}
	public String[] getPeerId(){
		return conference.getPeerIds();
	}
}
