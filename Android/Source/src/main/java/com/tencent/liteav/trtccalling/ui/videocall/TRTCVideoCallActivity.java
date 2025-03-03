package com.tencent.liteav.trtccalling.ui.videocall;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.basic.UserModel;
import com.tencent.liteav.trtccalling.R;
import com.tencent.liteav.trtccalling.model.TRTCCalling;
import com.tencent.liteav.trtccalling.model.TRTCCallingDelegate;
import com.tencent.liteav.trtccalling.model.impl.TRTCCallingImpl;
import com.tencent.liteav.trtccalling.model.impl.base.CallingInfoManager;
import com.tencent.liteav.trtccalling.ui.BaseCallActivity;
import com.tencent.liteav.trtccalling.ui.common.RoundCornerImageView;
import com.tencent.liteav.trtccalling.ui.common.Utils;
import com.tencent.liteav.trtccalling.ui.videocall.videolayout.TRTCVideoLayout;
import com.tencent.liteav.trtccalling.ui.videocall.videolayout.TRTCVideoLayoutManager;
import com.tencent.trtc.TRTCCloudDef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于展示视频通话的主界面，通话的接听和拒绝就是在这个界面中完成的。
 *
 * @author guanyifeng
 */
public class TRTCVideoCallActivity extends BaseCallActivity {
    public static final int TYPE_BEING_CALLED = 1;
    public static final int TYPE_CALL         = 2;

    public static final  String PARAM_TYPE                = "type";
    public static final  String PARAM_SELF_INFO           = "self_info";
    public static final  String PARAM_USER                = "user_model";
    public static final  String PARAM_BEINGCALL_USER      = "beingcall_user_model";
    public static final  String PARAM_OTHER_INVITING_USER = "other_inviting_user_model";
    private static final int    MAX_SHOW_INVITING_USER    = 4;

    private ImageView              mMuteImg;
    private ImageView              mSwitchCameraImg;
    private LinearLayout           mOpenCameraLl;
    private ImageView              mOpenCameraImg;
    private LinearLayout           mMuteLl;
    private ImageView              mHangupImg;
    private LinearLayout           mHangupLl;
    private ImageView              mHandsfreeImg;
    private LinearLayout           mHandsfreeLl;
    private ImageView              mDialingImg;
    private LinearLayout           mDialingLl;
    private TextView               mTvHangup;
    private TRTCVideoLayoutManager mLayoutManagerTrtc;
    private Group                  mInvitingGroup;
    private LinearLayout           mImgContainerLl;
    private TextView               mTimeTv;
    private RoundCornerImageView   mSponsorAvatarImg;
    private TextView               mSponsorUserNameTv;
    private TextView               mSponsorUserVideoTag;
    private View                   mViewSwitchAudioCall;
    private View                   mShadeSponsor;
    private Runnable               mTimeRunnable;
    private int                    mTimeCount;
    private Handler                mTimeHandler;
    private HandlerThread          mTimeHandlerThread;

    /**
     * 拨号相关成员变量
     */
    private UserInfo              mSelfModel;
    private List<UserInfo>        mCallUserInfoList = new ArrayList<>(); // 呼叫方
    private Map<String, UserInfo> mCallUserModelMap = new HashMap<>();
    private UserInfo              mSponsorUserInfo;                      // 被叫方
    private List<UserInfo>        mOtherInvitingUserInfoList;
    private int                   mCallType;
    private TRTCCalling           mTRTCCalling;
    private boolean               mIsHandsFree      = true;
    private boolean               mIsMuteMic        = false;
    private boolean               mIsFrontCamera    = true;
    private boolean               mIsCameraOpen     = true;
    private boolean               mIsAudioMode      = false;
    private boolean               mIsCalledClick    = false;  //被叫方点击转换语音


    /**
     * 拨号的回调
     */
    private TRTCCallingDelegate mTRTCCallingDelegate = new TRTCCallingDelegate() {
        @Override
        public void onError(int code, String msg) {
            //发生了错误，报错并退出该页面
            ToastUtils.showLong(getString(R.string.trtccalling_toast_call_error_msg, code, msg));
            stopCameraAndFinish();
        }

        @Override
        public void onInvited(String sponsor, List<String> userIdList, boolean isFromGroup, int callType) {
        }

        @Override
        public void onGroupCallInviteeListUpdate(List<String> userIdList) {
        }

        @Override
        public void onUserEnter(final String userId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showCallingView();
                    //1.先造一个虚拟的用户添加到屏幕上
                    UserInfo model = new UserInfo();
                    model.userId = userId;
                    model.userName = userId;
                    model.userAvatar = "";
                    mCallUserInfoList.add(model);
                    mCallUserModelMap.put(model.userId, model);
                    showVideoView(model);
                    CallingInfoManager.getInstance().getUserInfoByUserId(userId, new CallingInfoManager.UserCallback() {
                        @Override
                        public void onSuccess(UserModel model) {
                            TRTCVideoLayout layout = mLayoutManagerTrtc.findCloudViewView(model.userId);
                            if (layout != null) {
                                layout.getUserNameTv().setText(model.userName);
                                ImageLoader.loadImage(getApplicationContext(), layout.getHeadImg(), model.userAvatar,
                                        R.drawable.trtccalling_ic_avatar);
                            }
                        }

                        @Override
                        public void onFailed(int code, String msg) {
                            ToastUtils.showLong(getString(R.string.trtccalling_toast_search_fail, msg));
                        }
                    });

                }
            });
        }

        @Override
        public void onUserLeave(final String userId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //1. 回收界面元素
                    mLayoutManagerTrtc.recyclerCloudViewView(userId);
                    //2. 删除用户model
                    UserInfo userInfo = mCallUserModelMap.remove(userId);
                    if (userInfo != null) {
                        mCallUserInfoList.remove(userInfo);
                    }
                }
            });
        }

        @Override
        public void onReject(final String userId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCallUserModelMap.containsKey(userId)) {
                        // 进入拒绝环节
                        //1. 回收界面元素
                        mLayoutManagerTrtc.recyclerCloudViewView(userId);
                        //2. 删除用户model
                        UserInfo userInfo = mCallUserModelMap.remove(userId);
                        if (userInfo != null) {
                            mCallUserInfoList.remove(userInfo);
                            ToastUtils.showLong(getString(R.string.trtccalling_toast_user_reject_call, userInfo.userName));
                        }
                    }
                }
            });
        }

        @Override
        public void onNoResp(final String userId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCallUserModelMap.containsKey(userId)) {
                        // 进入无响应环节
                        //1. 回收界面元素
                        mLayoutManagerTrtc.recyclerCloudViewView(userId);
                        //2. 删除用户model
                        UserInfo userInfo = mCallUserModelMap.remove(userId);
                        if (userInfo != null) {
                            mCallUserInfoList.remove(userInfo);
                            ToastUtils.showLong(getString(R.string.trtccalling_toast_user_not_response, userInfo.userName));
                        }
                    }
                    stopCameraAndFinish();
                }
            });
        }

        @Override
        public void onLineBusy(String userId) {
            if (mCallUserModelMap.containsKey(userId)) {
                // 进入无响应环节
                //1. 回收界面元素
                mLayoutManagerTrtc.recyclerCloudViewView(userId);
                //2. 删除用户model
                UserInfo userInfo = mCallUserModelMap.remove(userId);
                if (userInfo != null) {
                    mCallUserInfoList.remove(userInfo);
                    ToastUtils.showLong(getString(R.string.trtccalling_toast_user_busy, userInfo.userName));
                }
            }
        }

        @Override
        public void onCallingCancel() {
            if (mSponsorUserInfo != null) {
                ToastUtils.showLong(getString(R.string.trtccalling_toast_user_cancel_call, mSponsorUserInfo.userName));
            }
            stopCameraAndFinish();
        }

        @Override
        public void onCallingTimeout() {
            if (mSponsorUserInfo != null) {
                ToastUtils.showLong(getString(R.string.trtccalling_toast_user_timeout, mSponsorUserInfo.userName));
            }
            stopCameraAndFinish();
        }

        @Override
        public void onCallEnd() {
            if (mSponsorUserInfo != null) {
                ToastUtils.showLong(getString(R.string.trtccalling_toast_user_end, mSponsorUserInfo.userName));
            }
            stopCameraAndFinish();
        }

        @Override
        public void onUserVideoAvailable(final String userId, final boolean isVideoAvailable) {
            //有用户的视频开启了
            TRTCVideoLayout layout = mLayoutManagerTrtc.findCloudViewView(userId);
            if (layout != null) {
                layout.setVideoAvailable(isVideoAvailable);
                if (isVideoAvailable) {
                    mTRTCCalling.startRemoteView(userId, layout.getVideoView());
                } else {
                    mTRTCCalling.stopRemoteView(userId);
                }
            } else {

            }
        }

        @Override
        public void onUserAudioAvailable(String userId, boolean isVideoAvailable) {

        }

        @Override
        public void onUserVoiceVolume(Map<String, Integer> volumeMap) {
            for (Map.Entry<String, Integer> entry : volumeMap.entrySet()) {
                String          userId = entry.getKey();
                TRTCVideoLayout layout = mLayoutManagerTrtc.findCloudViewView(userId);
                if (layout != null) {
                    layout.setAudioVolumeProgress(entry.getValue());
                }
            }
        }

        @Override
        public void onNetworkQuality(TRTCCloudDef.TRTCQuality localQuality, ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {
            updateNetworkQuality(localQuality, remoteQuality);
        }

        @Override
        public void onSwitchToAudio(boolean success, String message) {
            if (success) {
                updateAudioCallView();
                mIsAudioMode = true;
                if (mIsCalledClick && mCallType == TYPE_BEING_CALLED) {
                    mTRTCCalling.accept();
                }
                enableHandsFree(false);
            } else {
                Toast.makeText(TRTCVideoCallActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void updateAudioCallView() {
        mViewSwitchAudioCall.setVisibility(View.GONE);
        mOpenCameraLl.setVisibility(View.GONE);
        mSwitchCameraImg.setVisibility(View.GONE);
        ConstraintLayout.LayoutParams muteLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        int hangupLlId = mHangupLl.getId();
        muteLayoutParams.bottomToBottom = hangupLlId;
        muteLayoutParams.rightToLeft = hangupLlId;
        muteLayoutParams.rightMargin = Utils.dp2px(TRTCVideoCallActivity.this, 40);
        mMuteLl.setLayoutParams(muteLayoutParams);

        ConstraintLayout.LayoutParams handsFreeLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        handsFreeLayoutParams.bottomToBottom = hangupLlId;
        handsFreeLayoutParams.leftToRight = hangupLlId;
        handsFreeLayoutParams.leftMargin = Utils.dp2px(TRTCVideoCallActivity.this, 40);
        mHandsfreeLl.setLayoutParams(handsFreeLayoutParams);

        ConstraintLayout.LayoutParams timeLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        timeLayoutParams.bottomToTop = hangupLlId;
        timeLayoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        timeLayoutParams.leftToLeft  = ConstraintLayout.LayoutParams.PARENT_ID;
        timeLayoutParams.bottomMargin = Utils.dp2px(TRTCVideoCallActivity.this, 20);
        mTimeTv.setLayoutParams(timeLayoutParams);
        showRemoteUserView();
    }

    /**
     * 主动拨打给某个用户
     *
     * @param context
     * @param selfModel
     * @param searchModel
     */
    public static void startCallSomeone(Context context, UserModel selfModel,UserModel searchModel) {
        UserInfo selfInfo = new UserInfo();
        selfInfo.userId = selfModel.userId;
        selfInfo.userAvatar = selfModel.userAvatar;
        selfInfo.userName = selfModel.userName;
        List<UserInfo> callUserInfoList = new ArrayList<>();
        UserInfo       callUserInfo     = new UserInfo();
        callUserInfo.userId = searchModel.userId;
        callUserInfo.userAvatar = searchModel.userAvatar;
        callUserInfo.userName = searchModel.userName;
        callUserInfoList.add(callUserInfo);

        Intent starter = new Intent(context, TRTCVideoCallActivity.class);
        starter.putExtra(PARAM_TYPE, TYPE_CALL);
        starter.putExtra(PARAM_SELF_INFO, selfInfo);
        starter.putExtra(PARAM_USER, new IntentParams(callUserInfoList));
        context.startActivity(starter);
    }

    /**
     * 作为用户被叫
     *
     * @param context
     * @param beingCallUserInfo
     */
    public static void startBeingCall(Context context, UserInfo selfUserInfo, UserInfo beingCallUserInfo, List<UserInfo> otherInvitingUserInfo) {
        Intent starter = new Intent(context, TRTCVideoCallActivity.class);
        starter.putExtra(PARAM_TYPE, TYPE_BEING_CALLED);
        starter.putExtra(PARAM_SELF_INFO, selfUserInfo);
        starter.putExtra(PARAM_BEINGCALL_USER, beingCallUserInfo);
        starter.putExtra(PARAM_OTHER_INVITING_USER, new IntentParams(otherInvitingUserInfo));
        starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 应用运行时，保持不锁屏、全屏化
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.trtccalling_videocall_activity_call_main);
        initStatusBar();
        initView();
        initData();
        initListener();
    }

    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    public void onBackPressed() {
        mTRTCCalling.hangup();
        stopCameraAndFinish();
        super.onBackPressed();
    }

    private void stopCameraAndFinish() {
        mTRTCCalling.closeCamera();
        mTRTCCalling.removeDelegate(mTRTCCallingDelegate);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimeCount();
        mTimeHandlerThread.quit();
    }

    private void initListener() {
        mMuteLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsMuteMic = !mIsMuteMic;
                mTRTCCalling.setMicMute(mIsMuteMic);
                mMuteImg.setActivated(mIsMuteMic);
                ToastUtils.showLong(mIsMuteMic ? R.string.trtccalling_toast_enable_mute : R.string.trtccalling_toast_disable_mute);
            }
        });
        mSwitchCameraImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mIsCameraOpen){
                    ToastUtils.showShort(R.string.trtccalling_switch_camera_hint);
                    return;
                }
                mIsFrontCamera = !mIsFrontCamera;
                mTRTCCalling.switchCamera(mIsFrontCamera);
                mSwitchCameraImg.setActivated(mIsFrontCamera);
                ToastUtils.showLong(R.string.trtccalling_toast_switch_camera);
            }
        });

        mOpenCameraLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TRTCVideoLayout videoLayout = mLayoutManagerTrtc.findCloudViewView(mSelfModel.userId);
                if (videoLayout == null) {
                    return;
                }
                if (!mIsCameraOpen) {
                    mTRTCCalling.openCamera(mIsFrontCamera, videoLayout.getVideoView());
                    videoLayout.setVideoAvailable(true);
                    mIsCameraOpen = true;
                    mOpenCameraImg.setActivated(false);
                } else {
                    mTRTCCalling.closeCamera();
                    videoLayout.setVideoAvailable(false);
                    mIsCameraOpen = false;
                    mOpenCameraImg.setActivated(true);
                }
                ToastUtils.showLong(mIsCameraOpen ? R.string.trtccalling_toast_enable_camera : R.string.trtccalling_toast_disable_camera);
            }
        });
        mHandsfreeLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsHandsFree = !mIsHandsFree;
                mTRTCCalling.setHandsFree(mIsHandsFree);
                mHandsfreeImg.setActivated(mIsHandsFree);
                ToastUtils.showLong(mIsHandsFree ? R.string.trtccalling_toast_use_speaker : R.string.trtccalling_toast_use_handset);
            }
        });
        mMuteImg.setActivated(mIsMuteMic);
        mHandsfreeImg.setActivated(mIsHandsFree);
        mViewSwitchAudioCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTRTCCalling.switchToAudioCall();
                mIsCalledClick = true;
                enableHandsFree(false);
            }
        });
    }

    private void initData() {
        // 初始化成员变量
        mTRTCCalling = TRTCCallingImpl.sharedInstance(this);
        mTRTCCalling.addDelegate(mTRTCCallingDelegate);
        mTimeHandlerThread = new HandlerThread("time-count-thread");
        mTimeHandlerThread.start();
        mTimeHandler = new Handler(mTimeHandlerThread.getLooper());
        // 初始化从外界获取的数据
        Intent intent = getIntent();
        //自己的资料
        mSelfModel = (UserInfo) intent.getSerializableExtra(PARAM_SELF_INFO);
        mCallType = intent.getIntExtra(PARAM_TYPE, TYPE_BEING_CALLED);
        if (mCallType == TYPE_BEING_CALLED) {
            // 作为被叫
            mSponsorUserInfo = (UserInfo) intent.getSerializableExtra(PARAM_BEINGCALL_USER);
            IntentParams params = (IntentParams) intent.getSerializableExtra(PARAM_OTHER_INVITING_USER);
            if (params != null) {
                mOtherInvitingUserInfoList = params.mUserInfos;
            }
            PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.MICROPHONE).callback(new PermissionUtils.FullCallback() {
                @Override
                public void onGranted(List<String> permissionsGranted) {
                    showWaitingResponseView();
                }

                @Override
                public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                    mTRTCCalling.reject();
                    ToastUtils.showShort(R.string.trtccalling_tips_start_camera_audio);
                    finish();
                }
            }).request();
        } else {
            // 主叫方
            IntentParams params = (IntentParams) intent.getSerializableExtra(PARAM_USER);
            if (params != null) {
                mCallUserInfoList = params.mUserInfos;
                for (UserInfo userInfo : mCallUserInfoList) {
                    mCallUserModelMap.put(userInfo.userId, userInfo);
                }
                startInviting();
                showInvitingView();
            }
        }

    }

    private void startInviting() {
        final List<String> list = new ArrayList<>();
        for (UserInfo userInfo : mCallUserInfoList) {
            list.add(userInfo.userId);
        }
        PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.MICROPHONE).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                mTRTCCalling.groupCall(list, TRTCCalling.TYPE_VIDEO_CALL, "");
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                ToastUtils.showShort(R.string.trtccalling_tips_start_camera_audio);
                finish();
            }
        }).request();
    }

    private void initView() {
        mMuteImg = (ImageView) findViewById(R.id.iv_mute);
        mMuteLl = (LinearLayout) findViewById(R.id.ll_mute);
        mHangupImg = (ImageView) findViewById(R.id.iv_hangup);
        mHangupLl = (LinearLayout) findViewById(R.id.ll_hangup);
        mHandsfreeImg = (ImageView) findViewById(R.id.iv_handsfree);
        mHandsfreeLl = (LinearLayout) findViewById(R.id.ll_handsfree);
        mDialingImg = (ImageView) findViewById(R.id.iv_dialing);
        mDialingLl = (LinearLayout) findViewById(R.id.ll_dialing);
        mLayoutManagerTrtc = (TRTCVideoLayoutManager) findViewById(R.id.trtc_layout_manager);
        mInvitingGroup = (Group) findViewById(R.id.group_inviting);
        mImgContainerLl = (LinearLayout) findViewById(R.id.ll_img_container);
        mTimeTv = (TextView) findViewById(R.id.tv_time);
        mSponsorAvatarImg = (RoundCornerImageView) findViewById(R.id.iv_sponsor_avatar);
        mSponsorUserNameTv = (TextView) findViewById(R.id.tv_sponsor_user_name);
        mSwitchCameraImg = (ImageView) findViewById(R.id.switch_camera);
        mOpenCameraLl = (LinearLayout) findViewById(R.id.ll_open_camera);
        mOpenCameraImg = (ImageView) findViewById(R.id.img_camera);
        mSponsorUserVideoTag = (TextView) findViewById(R.id.tv_sponsor_video_tag);
        mViewSwitchAudioCall = findViewById(R.id.ll_switch_audio_call);
        mTvHangup = (TextView) findViewById(R.id.tv_hangup);
        mShadeSponsor = findViewById(R.id.shade_sponsor);
    }

    private void enableHandsFree(boolean enable) {
        mIsHandsFree = enable;
        mTRTCCalling.setHandsFree(mIsHandsFree);
        mHandsfreeImg.setActivated(mIsHandsFree);
    }

    /**
     * 等待接听界面
     */
    public void showWaitingResponseView() {
        //1. 展示自己的画面
        mLayoutManagerTrtc.setMySelfUserId(mSelfModel.userId);
        final TRTCVideoLayout videoLayout = addUserToManager(mSelfModel);
        if (videoLayout == null) {
            return;
        }
        videoLayout.setVideoAvailable(true);
        mTRTCCalling.openCamera(true, videoLayout.getVideoView());
        //2. 展示对方的头像和蒙层
        visibleSponsorGroup(true);
        ImageLoader.loadImage(getApplicationContext(), mSponsorAvatarImg, mSponsorUserInfo.userAvatar,
                R.drawable.trtccalling_ic_avatar);
        mSponsorUserNameTv.setText(mSponsorUserInfo.userName);

        //3. 展示电话对应界面
        mHangupLl.setVisibility(View.VISIBLE);
        mDialingLl.setVisibility(View.VISIBLE);
        mHandsfreeLl.setVisibility(View.GONE);
        mMuteLl.setVisibility(View.GONE);
        mSwitchCameraImg.setVisibility(View.GONE);
        mOpenCameraLl.setVisibility(View.GONE);
        mViewSwitchAudioCall.setVisibility(View.VISIBLE);
        //3. 设置对应的listener
        mHangupLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTRTCCalling.reject();
                stopCameraAndFinish();
            }
        });
        mDialingLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //2.接听电话
                mTRTCCalling.accept();
                showCallingView();
            }
        });
        //4. 展示其他用户界面
        showOtherInvitingUserView();
        mTvHangup.setText(R.string.trtccalling_text_reject);
    }

    private void visibleSponsorGroup(boolean visible) {
        if (visible) {
            mSponsorUserVideoTag.setVisibility(View.VISIBLE);
            mSponsorUserNameTv.setVisibility(View.VISIBLE);
            mSponsorAvatarImg.setVisibility(View.VISIBLE);
            mShadeSponsor.setVisibility(View.VISIBLE);
        } else {
            mSponsorUserVideoTag.setVisibility(View.GONE);
            mSponsorUserNameTv.setVisibility(View.GONE);
            mSponsorAvatarImg.setVisibility(View.GONE);
            mShadeSponsor.setVisibility(View.GONE);
        }
    }

    /**
     * 展示邀请列表
     */
    public void showInvitingView() {
        //1. 展示自己的界面
        mLayoutManagerTrtc.setMySelfUserId(mSelfModel.userId);
        final TRTCVideoLayout videoLayout = addUserToManager(mSelfModel);
        if (videoLayout == null) {
            return;
        }
        videoLayout.setVideoAvailable(true);
        mTRTCCalling.openCamera(true, videoLayout.getVideoView());
        //2. 设置底部栏
        mHangupLl.setVisibility(View.VISIBLE);
        mHangupLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTRTCCalling.hangup();
                stopCameraAndFinish();
            }
        });
        mDialingLl.setVisibility(View.GONE);
        mHandsfreeLl.setVisibility(View.GONE);
        mMuteLl.setVisibility(View.GONE);
        mSwitchCameraImg.setVisibility(View.GONE);
        mOpenCameraLl.setVisibility(View.GONE);
        mViewSwitchAudioCall.setVisibility(View.VISIBLE);
        //3. 隐藏中间他们也在界面
        hideOtherInvitingUserView();
        //4. sponsor画面也隐藏
        visibleSponsorGroup(true);
        mSponsorUserInfo = mCallUserInfoList.get(0);
        ImageLoader.loadImage(getApplicationContext(), mSponsorAvatarImg, mSponsorUserInfo.userAvatar,
                R.drawable.trtccalling_ic_avatar);
        mSponsorUserVideoTag.setText(getString(R.string.trtccalling_waiting_be_accepted));
        mSponsorUserNameTv.setText(mSponsorUserInfo.userName);
        mTvHangup.setText(R.string.trtccalling_text_hangup);
    }

    /**
     * 展示通话中的界面
     */
    public void showCallingView() {
        //1. 蒙版消失
        visibleSponsorGroup(false);
        //2. 底部状态栏
        mHangupLl.setVisibility(View.VISIBLE);
        mDialingLl.setVisibility(View.GONE);
        mHandsfreeLl.setVisibility(View.VISIBLE);
        mMuteLl.setVisibility(View.VISIBLE);
        mSwitchCameraImg.setVisibility(mIsAudioMode ? View.GONE : View.VISIBLE);
        mOpenCameraLl.setVisibility(mIsAudioMode ? View.GONE : View.VISIBLE);
        mViewSwitchAudioCall.setVisibility(mIsAudioMode ? View.GONE : View.VISIBLE);
        mHangupLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTRTCCalling.hangup();
                stopCameraAndFinish();
            }
        });
        showTimeCount();
        hideOtherInvitingUserView();
        mTvHangup.setText(R.string.trtccalling_text_hangup);
    }

    private void showTimeCount() {
        if (mTimeRunnable != null) {
            return;
        }
        mTimeCount = 0;
        mTimeTv.setText(getShowTime(mTimeCount));
        if (mTimeRunnable == null) {
            mTimeRunnable = new Runnable() {
                @Override
                public void run() {
                    mTimeCount++;
                    if (mTimeTv != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTimeTv.setText(getShowTime(mTimeCount));
                            }
                        });
                    }
                    mTimeHandler.postDelayed(mTimeRunnable, 1000);
                }
            };
        }
        mTimeHandler.postDelayed(mTimeRunnable, 1000);
    }

    private void stopTimeCount() {
        mTimeHandler.removeCallbacks(mTimeRunnable);
        mTimeRunnable = null;
    }

    private String getShowTime(int count) {
        return getString(R.string.trtccalling_called_time_format, count / 60, count % 60);
    }

    private void showOtherInvitingUserView() {
        if (mOtherInvitingUserInfoList == null || mOtherInvitingUserInfoList.size() == 0) {
            return;
        }
        mInvitingGroup.setVisibility(View.VISIBLE);
        int squareWidth = getResources().getDimensionPixelOffset(R.dimen.trtccalling_small_image_size);
        int leftMargin  = getResources().getDimensionPixelOffset(R.dimen.trtccalling_small_image_left_margin);
        for (int index = 0; index < mOtherInvitingUserInfoList.size() && index < MAX_SHOW_INVITING_USER; index++) {
            UserInfo                  userInfo     = mOtherInvitingUserInfoList.get(index);
            ImageView                 imageView    = new ImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(squareWidth, squareWidth);
            if (index != 0) {
                layoutParams.leftMargin = leftMargin;
            }
            imageView.setLayoutParams(layoutParams);
            ImageLoader.loadImage(getApplicationContext(), imageView, userInfo.userAvatar,
                    R.drawable.trtccalling_ic_avatar);
            mImgContainerLl.addView(imageView);
        }
    }

    private void hideOtherInvitingUserView() {
        mInvitingGroup.setVisibility(View.GONE);
    }

    private void showVideoView(final UserInfo userInfo) {
        TRTCVideoLayout videoLayout = addUserToManager(userInfo);
        if (videoLayout == null) {
            return;
        }
        videoLayout.setVideoAvailable(!mIsAudioMode);
        videoLayout.setRemoteIconAvailable(mIsAudioMode);
    }

    private void showRemoteUserView() {
        if (mCallUserInfoList.isEmpty()) {
            return;
        }
        UserInfo remoteUser = mCallUserInfoList.get(0);
        if (remoteUser == null) {
            return;
        }
        String userId = remoteUser.userId;
        TRTCVideoLayout videoLayout = mLayoutManagerTrtc.findCloudViewView(userId);
        if (videoLayout == null) {
            return;
        }
        videoLayout.setVideoAvailable(false);
        videoLayout.setRemoteIconAvailable(true);
    }

    private TRTCVideoLayout addUserToManager(UserInfo userInfo) {
        TRTCVideoLayout layout = mLayoutManagerTrtc.allocCloudVideoView(userInfo.userId);
        if (layout == null) {
            return null;
        }
        return layout;
    }

    private static class IntentParams implements Serializable {
        public List<UserInfo> mUserInfos;

        public IntentParams(List<UserInfo> userInfos) {
            mUserInfos = userInfos;
        }
    }
}
