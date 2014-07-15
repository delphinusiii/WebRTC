package fm.android.conference.webrtc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import fm.SingleAction;
import fm.icelink.Link;
import fm.icelink.webrtc.DefaultProviders;

public class MainActivity extends Activity {
	private RelativeLayout layout;
	private GestureDetector gestureDetector;
	private static RelativeLayout container;
	private Button btStartlocal, btStoplocal, btConnect, btStartRecord,
			btStopRecord, btGetPeer, btDisconnect;
	private ListView lv;
	private Adapter ad;
	final App app = App.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		// Get the activity's layout and give it a black background.
		layout = (RelativeLayout) findViewById(R.id.layout);
		layout.getRootView().setBackgroundColor(
				getResources().getColor(android.R.color.black));

		// Create a static container that will be preserved across
		// activity destruction/recreation.
		if (container == null) {
			container = new RelativeLayout(getApplicationContext());
			container.setLayoutParams(new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT));

			Toast.makeText(this, "Double-tap to switch camera.",
					Toast.LENGTH_SHORT).show();
		}

		// For demonstration purposes, use the double-tap gesture
		// to switch between the front and rear camera.
		gestureDetector = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {
					public boolean onDoubleTap(MotionEvent e) {
						App.getInstance().switchCamera();
						return true;
					}
				});

		// Android's video providers need a context
		// in order to create surface views for video
		// rendering, so we need to supply one before
		// we start up the local media.
		DefaultProviders.setAndroidContext(this);

		initActivity();
		btConnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				connectServer();

			}
		});

		btStartlocal.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startLocal();
			}
		});
		btStoplocal.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				stopLocal();
			}
		});
		btGetPeer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ad.removeAll();
				if(app.getPeerId().length>0){
					
					ad.addItem(app.getPeerId(),app.getLink());
				}else{
					Toast.makeText(MainActivity.this, "Empty", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		btDisconnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				disconnectServer();
			}
		});
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Information Peer ");
                final String peerId = ((Rowdata)ad.getItem(position)).getPeerId();
                final Link link = ((Rowdata)ad.getItem(position)).getLink();
                builder.setMessage("Peer ID: "+((Rowdata)ad.getItem(position)).getPeerId()+
                					"\nLink of peer: "+((Rowdata)ad.getItem(position)).getLink());
                builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                		app.startLocalMedia(new SingleAction<Exception>() {
                			public void invoke(Exception ex) {
                				if (ex == null) {
                					app.startConferenceWithRemoteVideoStream(link, peerId, container, new SingleAction<Exception>(){
                						public void invoke(Exception ex){
                							if(ex!=null){
                								alert("Could not start conference video. %s",
    										ex.getMessage());
                							}
                						}
                					});
                				} 
                			}
                		});
                    }
                });
                builder.show();
			}
		});

	}

	protected void onPause() {
		// Remove the static container from the current layout.
		layout.removeView(container);

		super.onPause();
	}

	protected void onResume() {
		super.onResume();

		// Add the static container to the current layout.
		layout.addView(container);
	}

	public boolean onTouchEvent(MotionEvent event) {
		// Handle the double-tap event.
		if (gestureDetector == null || !gestureDetector.onTouchEvent(event)) {
			return super.onTouchEvent(event);
		}
		return true;
	}

	public void alert(String format, Object... args) {
		final String text = String.format(format, args);
		final Activity self = this;
		self.runOnUiThread(new Runnable() {
			public void run() {
				AlertDialog.Builder alert = new AlertDialog.Builder(self);
				alert.setMessage(text);
				alert.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});
				alert.show();
			}
		});
	}

	private void initActivity() {

		btStartlocal = (Button) findViewById(R.id.Startlocal);
		btStoplocal = (Button) findViewById(R.id.Stoplocal);
		btConnect = (Button) findViewById(R.id.connect);
		btDisconnect = (Button) findViewById(R.id.disconnect);
		btStartRecord = (Button) findViewById(R.id.startrecord);
		btStopRecord = (Button) findViewById(R.id.stoprecord);
		btGetPeer = (Button) findViewById(R.id.getPeer);
		lv = (ListView) findViewById(R.id.listView);
		// startLocal();
	}

	public void startLocal() {
		btStartlocal.setVisibility(View.GONE);
		btStoplocal.setVisibility(View.VISIBLE);
		btConnect.setEnabled(true);
		app.startLocalMedia(new SingleAction<Exception>() {
			public void invoke(Exception ex) {
				if (ex == null) {
					app.getMediaToLayout(container,
							new SingleAction<Exception>() {
								public void invoke(Exception ex) {
									if (ex != null) {
										alert("Could not get local media. %s",
												ex.getMessage());
									}
								}
							});
				} else {

					alert("Could not start local media. %s", ex.getMessage());
				}
			}
		});
	}

	public void stopLocal() {
		// TODO Auto-generated method stub
		btStartlocal.setVisibility(View.VISIBLE);
		btStoplocal.setVisibility(View.GONE);
		btConnect.setVisibility(View.VISIBLE);
		btConnect.setEnabled(false);
		btDisconnect.setVisibility(View.GONE);
		btGetPeer.setEnabled(false);
		lv.setVisibility(View.GONE);
		ad.removeAll();
		container.removeAllViews();
		app.stopLocalMedia(new SingleAction<Exception>() {
			public void invoke(Exception ex) {
				app.stopConference();
				if (ex == null) {
					app.stopSignalling(new SingleAction<Exception>() {
						public void invoke(Exception ex) {
							if (ex != null)
								alert("Could not stop local media. %s",
										ex.getMessage());
						}
					});

				} else {
					alert("Could not stop local media. %s", ex.getMessage());
				}
			}
		});
	}

	public void connectServer() {
		ad = new Adapter();
		lv.setAdapter(ad);
		btConnect.setVisibility(View.GONE);
		btDisconnect.setVisibility(View.VISIBLE);
		btDisconnect.setEnabled(true);
		btGetPeer.setEnabled(true);
		lv.setVisibility(View.VISIBLE);
		app.startLocalMedia(new SingleAction<Exception>() {
			public void invoke(Exception ex) {
				if (ex == null) {
					// Start the signalling engine.
					app.startSignalling(new SingleAction<Exception>() {
						public void invoke(Exception ex) {
							if (ex != null) {
								alert("Could not start signalling. %s",
										ex.getMessage());
							}
						}
					});

					// Start the conference engine.
					app.startConference(container,
							new SingleAction<Exception>() {
								public void invoke(Exception ex) {
									if (ex != null) {
										alert("Could not start conference. %s",
												ex.getMessage());
									}
								}
							});
				} else {
					alert("Could not start local media. %s", ex.getMessage());
				}
			}
		});

	}

	public void disconnectServer() {
		btStartlocal.setVisibility(View.GONE);
		btStoplocal.setVisibility(View.VISIBLE);
		btConnect.setVisibility(View.VISIBLE);
		btConnect.setEnabled(true);
		btGetPeer.setEnabled(false);
		ad.removeAll();
		btDisconnect.setVisibility(View.GONE);
		lv.setVisibility(View.GONE);
		new SingleAction<Exception>() {
			public void invoke(Exception ex) {
				app.stopConference();
				app.stopSignalling(new SingleAction<Exception>() {
					public void invoke(Exception ex) {
						if (ex != null)
							alert("Could not stop signalling. %s",
									ex.getMessage());
					}
				});
			}
		};

	}
}
