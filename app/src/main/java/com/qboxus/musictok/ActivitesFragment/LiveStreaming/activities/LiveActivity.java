package com.qboxus.musictok.ActivitesFragment.LiveStreaming.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.CallBack;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.LiveCommentModel;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.LiveCommentsAdapter;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.LiveUserModel;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.LiveUserViewAdapter;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.stats.LocalStatsData;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.stats.RemoteStatsData;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.stats.StatsData;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.ui.VideoGridContainer;
import com.qboxus.musictok.ActivitesFragment.SendGift.StickerGiftF;
import com.qboxus.musictok.ActivitesFragment.SendGift.StickerModel;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.paperdb.Paper;
import thuannv.heartslayout.HeartDrawable;
import thuannv.heartslayout.HeartsLayout;

public class LiveActivity extends RtcBaseActivity implements View.OnClickListener {

    private VideoGridContainer mVideoGridContainer;
    ImageView ivChatView,ivJoinBc, ivGift;
    private VideoEncoderConfiguration.VideoDimensions mVideoDimension;
    DatabaseReference rootref;

    String userId, userName, userPicture;
    int userRole;
    EditText messageEdit;
    RelativeLayout bottom_tab_view,bottom_chat_view;
    TextView tvGiftCount;
    ImageView ivGiftCount;
    View animationCapture;
    LinearLayout tabGiftCount;
    LinearLayout tabLiveUser;
    HeartsLayout streamLikeView;
    private List<Drawable> mDrawables = new ArrayList<>();
    private Random mRandom = new Random();
    boolean isLikeStream=true;
    TextView tvCurrentJoin,liveUserCount;
    DrawerLayout drawer;
    ArrayList<String> alertList=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(LiveActivity.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, LiveActivity.class,false);
        setContentView(R.layout.activity_live_room);

        rootref = FirebaseDatabase.getInstance().getReference();
        ivChatView =findViewById(R.id.iv_chat_view);
        ivChatView.setOnClickListener(this);
        ivJoinBc =findViewById(R.id.ivJoinBc);
        ivJoinBc.setOnClickListener(this);
        ivGift =findViewById(R.id.iv_gift);
        ivGift.setOnClickListener(this);
        Intent bundle = getIntent();
        if (bundle != null) {
            userId = bundle.getStringExtra("user_id");
            userName = bundle.getStringExtra("user_name");
            userPicture = bundle.getStringExtra("user_picture");

            userRole = bundle.getIntExtra("user_role", Constants.CLIENT_ROLE_BROADCASTER);
        }

        if (userRole == Constants.CLIENT_ROLE_BROADCASTER) {

            rootref.child("LiveUsers").child(userId).keepSynced(true);
            rootref.child("LiveUsers").child(userId).onDisconnect().removeValue();
            ivJoinBc.setVisibility(View.VISIBLE);
            addFirebaseNode();
            sendLiveNotification();
        }
        else
        {
            ivJoinBc.setVisibility(View.GONE);
            listenerNode();
            AddJoinNode();
        }
        ListenerJoinNode();
        if(userId.equalsIgnoreCase(Variables.sharedPreferences.getString(Variables.U_ID,"")) ||
                !(Functions.getSharedPreference(LiveActivity.this).getBoolean(Variables.IsExtended, false)))
        {
            ivGift.setVisibility(View.GONE);
        }
        else {
            ivGift.setVisibility(View.VISIBLE);
        }
        tvGiftCount=findViewById(R.id.tvGiftCount);
        ivGiftCount=findViewById(R.id.ivGiftCount);
        tabGiftCount=findViewById(R.id.tabGiftCount);
        animationCapture=findViewById(R.id.animationCapture);
        tabLiveUser=findViewById(R.id.tabLiveUser);
        tabLiveUser.setOnClickListener(this);
        liveUserCount=findViewById(R.id.liveUserCount);
        tvCurrentJoin=findViewById(R.id.tvCurrentJoin);
        bottom_tab_view=findViewById(R.id.bottom_tab_view);
        bottom_chat_view=findViewById(R.id.bottom_chat_view);
        streamLikeView=findViewById(R.id.streamLikeView);
        streamLikeView.setOnClickListener(this);
        SetUpDrawerView();
        TextView live_user_name = findViewById(R.id.live_user_name);
        live_user_name.setText(userName);

        initUI();
        initData();

        messageEdit = findViewById(R.id.edtMessage);



        findViewById(R.id.cross_btn).setOnClickListener(this);
        findViewById(R.id.tvSend).setOnClickListener(this);



        getCommentData();
        initDrawables();

        if(com.qboxus.musictok.Constants.STREAMING_LIMIT)
        {
            try {
               if (userRole == Constants.CLIENT_ROLE_BROADCASTER)
               {
                   new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           LiveActivity.this.runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   Toast.makeText(LiveActivity.this,  getString(R.string.you_have_reached_out_of_your_streaming_limit), Toast.LENGTH_SHORT).show();
                                   finish();
                               }
                           });
                       }
                   },com.qboxus.musictok.Constants.MAX_STREMING_TIME);
               }
            }
            catch (Exception e)
            {
                Log.d(com.qboxus.musictok.Constants.tag,"Exception streaming : "+e);
            }
        }
    }

    ArrayList<LiveUserModel> jointUserList=new ArrayList<>();

    ValueEventListener joinValueEventListener;
    private void ListenerJoinNode() {
        joinValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                jointUserList.clear();
                if (dataSnapshot.exists())
                {
                    for(DataSnapshot joinSnapsot:dataSnapshot.getChildren())
                    {
                        Log.d(com.qboxus.musictok.Constants.tag,"Data JSON : "+joinSnapsot.getValue().toString());
                        if (!(TextUtils.isEmpty(joinSnapsot.getValue().toString())))
                        {
                            LiveUserModel model=joinSnapsot.getValue(LiveUserModel.class);
                            jointUserList.add(model);
                        }

                    }
                    liveUserViewAdapter.notifyDataSetChanged();
                    if (jointUserList.size()>0)
                    {
                        LiveUserModel currentModel=jointUserList.get(0);
                        tvCurrentJoin.setText(currentModel.getUser_name()+" "+getString(R.string.joined));
                        liveUserCount.setText(""+jointUserList.size());
                    }
                    else
                    {
                        liveUserCount.setText(""+jointUserList.size());
                    }

                }
                else
                {
                    liveUserCount.setText(""+jointUserList.size());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        rootref.child("LiveUsers").child(userId).child("JoinStream").addValueEventListener(joinValueEventListener);
    }

    public void removeJoinListener() {
        if (rootref != null && joinValueEventListener != null) {
            rootref.child("LiveUsers").child(userId).child("JoinStream").removeEventListener(joinValueEventListener);
        }
    }

    private void AddJoinNode() {
        HashMap map = new HashMap();
        map.put("user_id", Functions.getSharedPreference(LiveActivity.this).getString(Variables.U_ID, ""));
        map.put("user_name", Functions.getSharedPreference(LiveActivity.this).getString(Variables.U_NAME, ""));
        map.put("user_picture", Functions.getSharedPreference(LiveActivity.this).getString(Variables.U_PIC, ""));
        rootref.child("LiveUsers").child(userId).child("JoinStream")
                .child(Functions.getSharedPreference(LiveActivity.this).getString(Variables.U_ID, ""))
                .setValue(map);
    }

    private void initDrawables() {
        int[] resIds = {
                R.drawable.hand_blue,
                R.drawable.heart_blue,
        };

        Bitmap bitmap;
        Drawable drawable;
        Resources res = getResources();
        for (int id : resIds) {
            bitmap = BitmapFactory.decodeResource(res, id);
            drawable = new BitmapDrawable(res, bitmap);
            mDrawables.add(drawable);
        }

    }


    // initialize the views of activity
    private void initUI() {

        Functions.printLog(com.qboxus.musictok.Constants.tag, "initUI");

        initUserIcon();

        boolean isBroadcaster = (userRole == Constants.CLIENT_ROLE_BROADCASTER);


        isAudioActivated=!isBroadcaster;isVideoActivated=!isBroadcaster;
        isbeautyActivated=true;
        rtcEngine().setBeautyEffectOptions(isbeautyActivated,
                com.qboxus.musictok.ActivitesFragment.LiveStreaming.Constants.DEFAULT_BEAUTY_OPTIONS);

        mVideoGridContainer = findViewById(R.id.live_video_grid_layout);
        mVideoGridContainer.setStatsManager(statsManager());
        mVideoGridContainer.setOnClickListener(this);

        rtcEngine().setClientRole(userRole);
        if (isBroadcaster) startBroadcast();
    }

    // set the user profile picture
    private void initUserIcon() {
        Functions.printLog(com.qboxus.musictok.Constants.tag, "initUserIcon");
        SimpleDraweeView iconView = findViewById(R.id.live_name_board_icon);
        if (userPicture != null && !userPicture.equals("")) {
            iconView.setController(Functions.frescoImageLoad(userPicture,iconView,false));
        }
    }

    private void initData() {
        Functions.printLog(com.qboxus.musictok.Constants.tag, "initData");
        mVideoDimension = com.qboxus.musictok.ActivitesFragment.LiveStreaming.Constants.VIDEO_DIMENSIONS[
                config().getVideoDimenIndex()];
    }



    private void startBroadcast() {
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        SurfaceView surface = prepareRtcVideo(0, true);
        mVideoGridContainer.addUserVideoSurface(0, surface, true);
    }

    private void stopBroadcast() {
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        removeRtcVideo(0, true);
        mVideoGridContainer.removeUserVideo(0, true);
    }



    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        // Do nothing at the moment
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        // Do nothing at the moment
    }

    @Override
    public void onUserOffline(final int uid, int reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeRemoteUser(uid);
            }
        });
    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Functions.printLog(com.qboxus.musictok.Constants.tag, "onFirstRemoteVideoDecoded");
                renderRemoteUser(uid);
            }
        });
    }

    private void renderRemoteUser(int uid) {
        Functions.printLog(com.qboxus.musictok.Constants.tag, "renderRemoteUser");
        SurfaceView surface = prepareRtcVideo(uid, false);
        mVideoGridContainer.addUserVideoSurface(uid, surface, false);
    }

    private void removeRemoteUser(int uid) {
        removeRtcVideo(uid, false);
        mVideoGridContainer.removeUserVideo(uid, false);
    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setWidth(mVideoDimension.width);
        data.setHeight(mVideoDimension.height);
        data.setFramerate(stats.sentFrameRate);
    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        Functions.printLog(com.qboxus.musictok.Constants.tag, "onRtcStats");
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setLastMileDelay(stats.lastmileDelay);
        data.setVideoSendBitrate(stats.txVideoKBitRate);
        data.setVideoRecvBitrate(stats.rxVideoKBitRate);
        data.setAudioSendBitrate(stats.txAudioKBitRate);
        data.setAudioRecvBitrate(stats.rxAudioKBitRate);
        data.setCpuApp(stats.cpuAppUsage);
        data.setCpuTotal(stats.cpuAppUsage);
        data.setSendLoss(stats.txPacketLossRate);
        data.setRecvLoss(stats.rxPacketLossRate);
    }

    // check the network quality
    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        Functions.printLog(com.qboxus.musictok.Constants.tag, "onNetworkQuality");
        if (!statsManager().isEnabled()) return;

        StatsData data = statsManager().getStatsData(uid);
        if (data == null) return;

        data.setSendQuality(statsManager().qualityToString(txQuality));
        data.setRecvQuality(statsManager().qualityToString(rxQuality));
    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
        Functions.printLog(com.qboxus.musictok.Constants.tag, "onRemoteVideoStats");
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setWidth(stats.width);
        data.setHeight(stats.height);
        data.setFramerate(stats.rendererOutputFrameRate);
        data.setVideoDelay(stats.delay);
    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {
        Functions.printLog(com.qboxus.musictok.Constants.tag, "onRemoteAudioStats");
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setAudioNetDelay(stats.networkTransportDelay);
        data.setAudioNetJitter(stats.jitterBufferDelay);
        data.setAudioLoss(stats.audioLossRate);
        data.setAudioQuality(statsManager().qualityToString(stats.quality));
    }

    @Override
    public void finish() {
        super.finish();
        statsManager().clearAllData();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRole == Constants.CLIENT_ROLE_BROADCASTER) {
            removeNode();
            removeJoinListener();
        } else {
            removeJoinListener();
            removeJoinNode();
            removeListener();
        }

        removeCommentListener();


    }

    public void addFirebaseNode() {
        HashMap map = new HashMap();
        map.put("user_id", userId);
        map.put("user_name", userName);
        map.put("user_picture", userPicture);
        rootref.child("LiveUsers").child(userId).setValue(map);
    }

    // when user goes to offline then change the value status on firebase
    public void removeNode() {
        rootref.child("LiveUsers").child(userId).removeValue();
    }

    // when user goes to offline then change the value status on firebase
    public void removeJoinNode() {
        rootref.child("LiveUsers").child(userId).child("JoinStream")
                .child(Functions.getSharedPreference(LiveActivity.this).getString(Variables.U_ID, ""))
                .removeValue();
    }


    // check the current live user status eighter user is live or not when users goes offline this callback will hit
    ValueEventListener valueEventListener;

    public void listenerNode() {

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) {
                    finish();
                }
                else
                {

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        rootref.child("LiveUsers").child(userId).addValueEventListener(valueEventListener);
    }

    public void removeListener() {
        if (rootref != null && valueEventListener != null) {
            rootref.child("LiveUsers").child(userId).removeEventListener(valueEventListener);
        }
    }


    @Override
    public void onBackPressed() {
        if (bottom_chat_view.getVisibility()==View.VISIBLE)
        {
            bottom_chat_view.setVisibility(View.INVISIBLE);
            bottom_tab_view.setVisibility(View.VISIBLE);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cross_btn:
            {
                if (bottom_chat_view.getVisibility()==View.VISIBLE)
                {
                    bottom_chat_view.setVisibility(View.INVISIBLE);
                    bottom_tab_view.setVisibility(View.VISIBLE);
                }
                else
                {
                   finish();
                }
            }
                break;
            case R.id.tvSend:

                if (!TextUtils.isEmpty(messageEdit.getText().toString())) {
                   addMessages("comment");
                }
                break;
            case R.id.live_video_grid_layout:
            {
                if(!(userId.equalsIgnoreCase(Variables.sharedPreferences.getString(Variables.U_ID,""))))
                {
                    if (isLikeStream)
                    {
                        addLikeComment("like");
                        isLikeStream=false;
                    }
                }

            }
            break;
            case R.id.iv_gift:
            {
                ShowGiftSheet();
            }
            break;
            case R.id.iv_chat_view:
            {
                if (bottom_chat_view.getVisibility()==View.INVISIBLE)
                {
                    bottom_chat_view.setVisibility(View.VISIBLE);
                    bottom_tab_view.setVisibility(View.INVISIBLE);
                }
                else
                {
                    bottom_chat_view.setVisibility(View.INVISIBLE);
                    bottom_tab_view.setVisibility(View.VISIBLE);
                }
            }
            break;
            case R.id.streamLikeView:
            {
                int idx = mRandom.nextInt(mDrawables.size());
                Drawable drawable = mDrawables.get(idx);
                streamLikeView.add(new HeartDrawable(drawable));
            }
            break;
            case R.id.tabLiveUser:
            {
                drawer.openDrawer(GravityCompat.END);
            }
            break;
            case R.id.ivJoinBc:
            {
                ShowDailogForJoinBroadcast();
            }
            break;
        }
    }



    boolean isAudioActivated=true,isVideoActivated=true,isbeautyActivated=true;
    private void ShowDailogForJoinBroadcast() {
        final Dialog alertDialog = new Dialog(LiveActivity.this);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.live_join_broadcast_view);
        alertDialog.getWindow().setBackgroundDrawable(LiveActivity.this.getDrawable(R.drawable.d_round_white_background));

        ImageView swith_camera_btn=alertDialog.findViewById(R.id.swith_camera_btn);
        ImageView live_btn_mute_audio=alertDialog.findViewById(R.id.live_btn_mute_audio);
        ImageView live_btn_beautification=alertDialog.findViewById(R.id.live_btn_beautification);
        ImageView live_btn_mute_video=alertDialog.findViewById(R.id.live_btn_mute_video);
        LinearLayout tab_cancel = alertDialog.findViewById(R.id.tab_cancel);

        live_btn_mute_audio.setActivated(!isAudioActivated);
        live_btn_mute_video.setActivated(!isVideoActivated);
        live_btn_beautification.setActivated(!isbeautyActivated);

        rtcEngine().setBeautyEffectOptions(live_btn_mute_video.isActivated(),
                com.qboxus.musictok.ActivitesFragment.LiveStreaming.Constants.DEFAULT_BEAUTY_OPTIONS);

        tab_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        swith_camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                rtcEngine().switchCamera();
            }
        });
        live_btn_mute_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                isAudioActivated=live_btn_mute_video.isActivated();
                if (!isAudioActivated) return;
                rtcEngine().muteLocalAudioStream(isAudioActivated);
                view.setActivated(!isAudioActivated);
            }
        });
        live_btn_beautification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                isbeautyActivated=view.isActivated();
                view.setActivated(!isbeautyActivated);
                rtcEngine().setBeautyEffectOptions(isbeautyActivated,
                        com.qboxus.musictok.ActivitesFragment.LiveStreaming.Constants.DEFAULT_BEAUTY_OPTIONS);
            }
        });
        live_btn_mute_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                isVideoActivated=view.isActivated();
                if (isVideoActivated) {
                    stopBroadcast();
                } else {
                    startBroadcast();
                }
                view.setActivated(!isVideoActivated);
            }
        });
        alertDialog.show();
    }

    private void ShowGiftSheet() {
        StickerGiftF giftFragment = new StickerGiftF(userId, userName, userPicture, new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                if (bundle.getBoolean("isShow",false))
                {
                    StickerModel model= (StickerModel) bundle.getSerializable("Data");
                    String counter=bundle.getString("count");
                    addGiftComment("gift",counter,model);
                }
                else
                {
                    if (bundle.getBoolean("showCount",false))
                    {

                        StickerModel model= (StickerModel) bundle.getSerializable("Data");
                        tvGiftCount.setText(" X "+bundle.getString("count")+" "+ model.name);
                        Glide.with(LiveActivity.this).load(model.image)
                                .error(R.drawable.ic_gift_send)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(ivGiftCount);

                        tabGiftCount.animate().translationY(animationCapture.getY()).setDuration(700).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                tabGiftCount.setAlpha(1);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                tabGiftCount.setAlpha(0);

                            }
                        }).start();

                    }
                }
            }
        });
        giftFragment.show(getSupportFragmentManager(), "");
    }


    SimpleDraweeView ivGiftProfile;
    LinearLayout tabGiftTitle;
    ImageView ivGiftItem;
    RelativeLayout tabGiftMain;
    View animationGiftCapture;
    TextView tvGiftTitle,tvGiftCountTitle,tvSendGiftCount;
    private void ShowGiftAnimation(LiveCommentModel item) {
        ivGiftProfile=findViewById(R.id.ivGiftProfile);
        tabGiftTitle=findViewById(R.id.tabGiftTitle);
        tabGiftMain=findViewById(R.id.tabGiftMain);
        tvGiftTitle=findViewById(R.id.tvGiftTitle);
        tvGiftCountTitle=findViewById(R.id.tvGiftCountTitle);
        ivGiftItem=findViewById(R.id.ivGiftItem);
        tvSendGiftCount=findViewById(R.id.tvSendGiftCount);
        animationGiftCapture=findViewById(R.id.animationGiftCapture);

        String[] str=item.getComment().split("=====");

        Uri imageUri = Uri.parse(str[2]);

        ivGiftProfile.setController(Functions.frescoImageLoad(item.getUserPicture(),ivGiftProfile,false));


        Glide.with(LiveActivity.this).load(imageUri)
                .error(R.drawable.ic_gift_send)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivGiftItem);
        tvGiftTitle.setText(item.getUserName());
        tvGiftCountTitle.setText(getString(R.string.gave_you_a)+" "+str[1]);
        tvSendGiftCount.setText("X "+str[0]);

        tabGiftMain.animate().translationX(animationGiftCapture.getX()).setDuration(1500).setListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        tabGiftMain.animate().translationY(animationCapture.getY()).setDuration(700).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                tabGiftMain.animate().translationY(0).translationX(0);
                                tabGiftMain.setAlpha(0);

                            }
                        }).start();
                    }
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        tabGiftMain.setAlpha(1);
                        PlayGiftSound();
                    }
                }).start();
    }

    MediaPlayer player;
    Handler handler;
    private void PlayGiftSound() {
        handler=new Handler(Looper.getMainLooper());
        player = MediaPlayer.create(getApplicationContext(), R.raw.gift_tone);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setVolume(100,100);
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        handler.postDelayed(runnable,2000);
    }

    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            onTuneStop();
        }
    };

    public void onTuneStop() {
        if(player!=null && player.isPlaying()){
            player.stop();
        }
        if (handler!=null)
        {
            handler.removeCallbacks(runnable);
        }
    }


    ArrayList<LiveCommentModel> dataList=new ArrayList<>();
    RecyclerView recyclerView;
    LiveCommentsAdapter adapter;
    RecyclerView liveUserViewRecyclerView;
    TextView tvNoViewData;
    LiveUserViewAdapter liveUserViewAdapter;

    // initailze the adapter
    public void initAdapter() {
        dataList.clear();
        alertList.clear();
        alertList.add(getString(R.string.streaming_instruction_one));
        alertList.add(getString(R.string.streaming_instruction_two));
        alertList.add(getString(R.string.streaming_instruction_three)+" "+getString(R.string.app_name)+". "+getString(R.string.for_more_info_check_support_services));

        recyclerView = (RecyclerView) findViewById(R.id.recylerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setHasFixedSize(true);

        adapter = new LiveCommentsAdapter(this, dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

            }
        });

        recyclerView.setAdapter(adapter);

        int idx = mRandom.nextInt(alertList.size());
        addMessagesToList("alert",alertList.get(idx));
    }

    private void SetUpDrawerView() {
        drawer = findViewById(R.id.drawer);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                drawer.openDrawer(GravityCompat.END);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        tvNoViewData=findViewById(R.id.tvNoViewData);
        liveUserViewRecyclerView=findViewById(R.id.liveUserViewRecyclerView);
        LinearLayoutManager layoutManager=new LinearLayoutManager(LiveActivity.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        liveUserViewRecyclerView.setLayoutManager(layoutManager);
        liveUserViewAdapter=new LiveUserViewAdapter(jointUserList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

            }
        });
        liveUserViewRecyclerView.setAdapter(liveUserViewAdapter);
    }

    // send the comment to the live user
    public void addMessages(String type) {

        DatabaseReference dref = rootref.child("LiveUsers").child(userId).child("Chat").push();

        final String key = dref.getKey();
        String my_id = Functions.getSharedPreference(this).getString(Variables.U_ID, "");
        String my_name = Functions.getSharedPreference(this).getString(Variables.F_NAME, "") + " " + Functions.getSharedPreference(this).getString(Variables.L_NAME, "");
        String my_image = Functions.getSharedPreference(this).getString(Variables.U_PIC, "null");

        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        LiveCommentModel commentItem=new LiveCommentModel();
        commentItem.setKey(key);
        commentItem.setUserId(my_id);
        commentItem.setUserName(my_name);
        commentItem.setUserPicture(my_image);
        commentItem.setComment(""+messageEdit.getText().toString());
        commentItem.setType(type);
        commentItem.setCommentTime(formattedDate);
        rootref.child("LiveUsers").child(userId).child("Chat").child(key).setValue(commentItem);

        messageEdit.setText(null);

    }


    // send the comment to the live user
    public void addLikeComment(String type) {

        DatabaseReference dref = rootref.child("LiveUsers").child(userId).child("Chat").push();

        final String key = dref.getKey();
        String my_id = Functions.getSharedPreference(this).getString(Variables.U_ID, "");
        String my_name = Functions.getSharedPreference(this).getString(Variables.F_NAME, "") + " " + Functions.getSharedPreference(this).getString(Variables.L_NAME, "");
        String my_image = Functions.getSharedPreference(this).getString(Variables.U_PIC, "null");

        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        LiveCommentModel commentItem=new LiveCommentModel();
        commentItem.setKey(key);
        commentItem.setUserId(my_id);
        commentItem.setUserName(my_name);
        commentItem.setUserPicture(my_image);
        commentItem.setComment(my_name+" "+getString(R.string.like_this_stream));
        commentItem.setType(type);
        commentItem.setCommentTime(formattedDate);
        rootref.child("LiveUsers").child(userId).child("Chat").child(key).setValue(commentItem);

        messageEdit.setText(null);

    }


    // send the comment to the live user
    public void addGiftComment(String type, String count, StickerModel model) {

        DatabaseReference dref = rootref.child("LiveUsers").child(userId).child("Chat").push();

        final String key = dref.getKey();
        String my_id = Functions.getSharedPreference(this).getString(Variables.U_ID, "");
        String my_name = Functions.getSharedPreference(this).getString(Variables.F_NAME, "") + " " + Functions.getSharedPreference(this).getString(Variables.L_NAME, "");
        String my_image = Functions.getSharedPreference(this).getString(Variables.U_PIC, "null");

        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        LiveCommentModel commentItem=new LiveCommentModel();
        commentItem.setKey(key);
        commentItem.setUserId(my_id);
        commentItem.setUserName(my_name);
        commentItem.setUserPicture(my_image);
        commentItem.setComment(count+"====="+model.name+"====="+model.image);
        commentItem.setType(type);
        commentItem.setCommentTime(formattedDate);
        rootref.child("LiveUsers").child(userId).child("Chat").child(key).setValue(commentItem);

        messageEdit.setText(null);

    }


    // send the init alert to the live user
    public void addMessagesToList(String type,String message) {

        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        LiveCommentModel commentItem=new LiveCommentModel();
        commentItem.setKey("");
        commentItem.setUserId("");
        commentItem.setUserName("");
        commentItem.setUserPicture("");
        commentItem.setComment(message);
        commentItem.setType(type);
        commentItem.setCommentTime(formattedDate);

        dataList.add(commentItem);
        adapter.notifyDataSetChanged();
    }

    ChildEventListener childEventListener;
    Calendar current_cal;
    public void getCommentData() {
        initAdapter();
        current_cal = Calendar.getInstance();
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                LiveCommentModel model = dataSnapshot.getValue(LiveCommentModel.class);
                dataList.add(model);

                if (Functions.checkTimeDiffernce(current_cal,model.getCommentTime()))
                {
                    if(model.getType().equalsIgnoreCase("gift"))
                    {
                        LiveActivity.super.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ShowGiftAnimation(model);
                            }
                        });
                    }
                }


                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(dataList.size() - 1);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        rootref.child("LiveUsers").child(userId).child("Chat").addChildEventListener(childEventListener);
    }

    public void removeCommentListener() {
        if (rootref != null && childEventListener != null)
            rootref.child("LiveUsers").child(userId).child("Chat").removeEventListener(childEventListener);

    }


    // send notification to all of it follower when user live
    public void sendLiveNotification() {
        JSONObject params = new JSONObject();
        try {
            params.put("user_id", Functions.getSharedPreference(this).getString(Variables.U_ID, ""));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(this, ApiLinks.sendLiveStreamPushNotfication, params, null);

    }



}
