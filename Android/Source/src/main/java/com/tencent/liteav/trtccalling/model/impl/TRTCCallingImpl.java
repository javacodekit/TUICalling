package com.tencent.liteav.trtccalling.model.impl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMOfflinePushInfo;
import com.tencent.imsdk.v2.V2TIMSDKConfig;
import com.tencent.imsdk.v2.V2TIMSDKListener;
import com.tencent.imsdk.v2.V2TIMSendCallback;
import com.tencent.imsdk.v2.V2TIMSignalingListener;
import com.tencent.imsdk.v2.V2TIMUserFullInfo;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.liteav.basic.UserModel;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.liteav.trtccalling.R;
import com.tencent.liteav.trtccalling.model.TRTCCalling;
import com.tencent.liteav.trtccalling.model.TRTCCallingCallback;
import com.tencent.liteav.trtccalling.model.TRTCCallingDelegate;
import com.tencent.liteav.trtccalling.model.impl.base.CallModel;
import com.tencent.liteav.trtccalling.model.impl.base.MessageCustom;
import com.tencent.liteav.trtccalling.model.impl.base.OfflineMessageBean;
import com.tencent.liteav.trtccalling.model.impl.base.OfflineMessageContainerBean;
import com.tencent.liteav.trtccalling.model.impl.base.SignallingData;
import com.tencent.liteav.trtccalling.model.impl.base.TRTCInternalListenerManager;
import com.tencent.liteav.trtccalling.model.impl.base.TRTCLogger;
import com.tencent.liteav.trtccalling.model.impl.util.MediaPlayHelper;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 视频/语音通话的具体实现
 * 本功能使用腾讯云实时音视频 / 腾讯云即时通信IM 组合实现
 */
public class TRTCCallingImpl extends TRTCCalling {
    private static final String TAG            = "TRTCCallingImpl";
    /**
     * 超时时间，单位秒
     */
    public static final  int    TIME_OUT_COUNT = 30;

    /**
     * room id 的取值范围
     */
    private static final int     ROOM_ID_MIN = 1;
    private static final int     ROOM_ID_MAX = Integer.MAX_VALUE;
    private final        Context mContext;

    /**
     * 底层SDK调用实例
     */
    private TRTCCloud mTRTCCloud;

    /**
     * 当前IM登录用户名
     */
    private String       mCurUserId            = "";
    private int          mSdkAppId;
    private String       mCurUserSig;
    /**
     * 是否首次邀请
     */
    private boolean      isOnCalling           = false;
    private String       mCurCallID            = "";
    private int          mCurRoomID            = 0;
    /**
     * 当前是否在TRTC房间中
     */
    private boolean      mIsInRoom             = false;
    private long         mEnterRoomTime        = 0;
    /**
     * 当前邀请列表
     * C2C通话时会记录自己邀请的用户
     * IM群组通话时会同步群组内邀请的用户
     * 当用户接听、拒绝、忙线、超时会从列表中移除该用户
     */
    private List<String> mCurInvitedList       = new ArrayList<>();
    /**
     * 当前语音通话中的远端用户
     */
    private Set<String>  mCurRoomRemoteUserSet = new HashSet<>();

    /**
     * C2C通话的邀请人
     * 例如A邀请B，B存储的mCurSponsorForMe为A
     */
    private String mCurSponsorForMe = "";

    /**
     * 当前通话的类型
     */
    private int                         mCurCallType   = TYPE_UNKNOWN;
    /**
     * 当前群组通话的群组ID
     */
    private String                      mCurGroupId    = "";
    /**
     * 最近使用的通话信令，用于快速处理
     */
    private CallModel                   mLastCallModel = new CallModel();
    /**
     * 上层传入回调
     */
    private TRTCInternalListenerManager mTRTCInternalListenerManager;
    private String                      mNickName;
    private String                      mFaceUrl;

    private boolean mIsUseFrontCamera;

    private boolean mWaitingLastActivityFinished;
    private boolean mIsInitIMSDK;

    private MediaPlayHelper mMediaPlayHelper;        // 音效

    private SensorManager       mSensorManager;
    private SensorEventListener mSensorEventListener;

    private boolean mIsBeingCalled = true;


    public boolean isWaitingLastActivityFinished() {
        return mWaitingLastActivityFinished;
    }

    public void setWaitingLastActivityFinished(boolean waiting) {
        mWaitingLastActivityFinished = waiting;
    }

    /**
     * 信令监听器
     */
    private V2TIMSignalingListener mTIMSignallingListener = new V2TIMSignalingListener() {
        @Override
        public void onReceiveNewInvitation(String inviteID, String inviter, String groupID, List<String> inviteeList, String data) {
            TRTCLogger.d(TAG, "onReceiveNewInvitation inviteID:" + inviteID + ", inviter:" + inviter + " data:" + data);
            SignallingData signallingData = convert2CallingData(data);
            if (!isCallingData(signallingData)) {
                TRTCLogger.d(TAG, "this is not the calling sense ");
                return;
            }
            processInvite(inviteID, inviter, groupID, inviteeList, signallingData);
        }

        @Override
        public void onInviteeAccepted(String inviteID, String invitee, String data) {
            TRTCLogger.d(TAG, "onInviteeAccepted inviteID:" + inviteID + ", invitee:" + invitee + " data:" + data);
            SignallingData signallingData = convert2CallingData(data);
            if (!isCallingData(signallingData)) {
                TRTCLogger.d(TAG, "this is not the calling sense ");
                return;
            }
            if (isSwitchAudioData(signallingData)) {
                realSwitchToAudioCall();
                return;
            }
            mCurInvitedList.remove(invitee);
        }

        @Override
        public void onInviteeRejected(String inviteID, String invitee, String data) {
            TRTCLogger.d(TAG, "onInviteeRejected inviteID:" + inviteID + ", invitee:" + invitee + " data:" + data);
            SignallingData signallingData = convert2CallingData(data);
            if (!isCallingData(signallingData)) {
                TRTCLogger.d(TAG, "this is not the calling sense ");
                return;
            }
            if (isSwitchAudioData(signallingData)) {
                String message = getSwitchAudioRejectMessage(signallingData);
                onSwitchToAudio(false, message);
                return;
            }
            if (!TextUtils.isEmpty(mCurCallID) && mCurCallID.equals(inviteID)) {
                mCurInvitedList.remove(invitee);
                mCurRoomRemoteUserSet.remove(invitee);
                if (isLineBusy(signallingData)) {
                    if (mTRTCInternalListenerManager != null) {
                        mTRTCInternalListenerManager.onLineBusy(invitee);
                    }
                } else {
                    if (mTRTCInternalListenerManager != null) {
                        mTRTCInternalListenerManager.onReject(invitee);
                    }
                }
                preExitRoom(null);
                stopDialingMusic();
                unregisterSensorEventListener();
            }
        }

        @Override
        public void onInvitationCancelled(String inviteID, String inviter, String data) {
            TRTCLogger.d(TAG, "onInvitationCancelled inviteID:" + inviteID + " data:" + data);
            SignallingData signallingData = convert2CallingData(data);
            if (!isCallingData(signallingData)) {
                TRTCLogger.d(TAG, "this is not the calling sense ");
                return;
            }
            if (inviteID.equals(mCurCallID)) {
                playHangupMusic();
                stopCall();
                if (mTRTCInternalListenerManager != null) {
                    mTRTCInternalListenerManager.onCallingCancel();
                }
            }
        }

        @Override
        public void onInvitationTimeout(String inviteID, List<String> inviteeList) {
            TRTCLogger.d(TAG, "onInvitationTimeout inviteID:" + inviteID);
            if (!inviteID.equals(mCurCallID)) {
                return;
            }
            if (TextUtils.isEmpty(mCurSponsorForMe)) {
                // 邀请者
                for (String userID : inviteeList) {
                    if (mTRTCInternalListenerManager != null) {
                        mTRTCInternalListenerManager.onNoResp(userID);
                    }
                    mCurInvitedList.remove(userID);
                }
                stopDialingMusic();
            } else {
                // 被邀请者
                if (inviteeList.contains(mCurUserId)) {
                    stopCall();
                    if (mTRTCInternalListenerManager != null) {
                        mTRTCInternalListenerManager.onCallingTimeout();
                    }
                }
                mCurInvitedList.removeAll(inviteeList);
            }
            // 每次超时都需要判断当前是否需要结束通话
            preExitRoom(null);
            playHangupMusic();
            unregisterSensorEventListener();
        }
    };

    private boolean isLineBusy(SignallingData signallingData) {
        if (isNewSignallingVersion(signallingData)) {
            SignallingData.DataInfo dataInfo = signallingData.getData();
            if (dataInfo == null) {
                return false;
            }
            return CallModel.VALUE_MSG_LINE_BUSY.equals(dataInfo.getMessage());
        }
        return CallModel.SIGNALING_EXTRA_KEY_LINE_BUSY.equals(signallingData.getLineBusy());
    }

    //是否是6-30改造后的信令版本
    private boolean isNewSignallingVersion(SignallingData signallingData) {
        return !TextUtils.isEmpty(signallingData.getPlatform()) && !TextUtils.isEmpty(signallingData.getBusinessID());
    }

    private String getSwitchAudioRejectMessage(SignallingData signallingData) {
        if (isNewSignallingVersion(signallingData)) {
            SignallingData.DataInfo dataInfo = signallingData.getData();
            if (dataInfo == null) {
                return "";
            }
            return dataInfo.getMessage();
        }
        String message = signallingData.getSwitchToAudioCall();
        return TextUtils.isEmpty(message) ? "" : message;
    }

    private boolean isCallingData(SignallingData signallingData) {
        //如果是老版本直接反馈true
        if (!isNewSignallingVersion(signallingData)) {
            return true;
        }
        String businessId = signallingData.getBusinessID();
        return CallModel.VALUE_BUSINESS_ID.equals(businessId);
    }

    private boolean isSwitchAudioData(SignallingData signallingData) {
        if (!isNewSignallingVersion(signallingData)) {
            return !TextUtils.isEmpty(signallingData.getSwitchToAudioCall());
        }
        if (signallingData.getData() == null) {
            return false;
        }
        return CallModel.VALUE_CMD_SWITCH_TO_AUDIO.equals(signallingData.getData().getCmd());
    }

    private SignallingData convert2CallingData(String data) {
        SignallingData signallingData = new SignallingData();
        Map<String, Object> extraMap;
        try {
            extraMap = new Gson().fromJson(data, Map.class);
            if (extraMap == null) {
                TRTCLogger.e(TAG, "onReceiveNewInvitation extraMap is null, ignore");
                return signallingData;
            }
            if (extraMap.containsKey(CallModel.KEY_VERSION)) {
                Object version = extraMap.get(CallModel.KEY_VERSION);
                if (version instanceof Double) {
                    signallingData.setVersion(((Double) version).intValue());
                } else {
                    TRTCLogger.e(TAG, "version is not Double, value is :" + version);
                }
            }

            if (extraMap.containsKey(CallModel.KEY_PLATFORM)) {
                Object platform = extraMap.get(CallModel.KEY_PLATFORM);
                if (platform instanceof String) {
                    signallingData.setPlatform((String) platform);
                } else {
                    TRTCLogger.e(TAG, "platform is not string, value is :" + platform);
                }
            }

            if (extraMap.containsKey(CallModel.KEY_BUSINESS_ID)) {
                Object businessId = extraMap.get(CallModel.KEY_BUSINESS_ID);
                if (businessId instanceof String) {
                    signallingData.setBusinessID((String) businessId);
                } else {
                    TRTCLogger.e(TAG, "businessId is not string, value is :" + businessId);
                }
            }

            //兼容老版本某些字段
            if (extraMap.containsKey(CallModel.SIGNALING_EXTRA_KEY_CALL_TYPE)) {
                Object callType = extraMap.get(CallModel.SIGNALING_EXTRA_KEY_CALL_TYPE);
                if (callType instanceof Double) {
                    signallingData.setCallType(((Double) callType).intValue());
                } else {
                    TRTCLogger.e(TAG, "callType is not Double, value is :" + callType);
                }
            }

            if (extraMap.containsKey(CallModel.SIGNALING_EXTRA_KEY_ROOM_ID)) {
                Object roomId = extraMap.get(CallModel.SIGNALING_EXTRA_KEY_ROOM_ID);
                if (roomId instanceof Double) {
                    signallingData.setRoomId(((Double) roomId).intValue());
                } else {
                    TRTCLogger.e(TAG, "roomId is not Double, value is :" + roomId);
                }
            }

            if (extraMap.containsKey(CallModel.SIGNALING_EXTRA_KEY_LINE_BUSY)) {
                Object lineBusy = extraMap.get(CallModel.SIGNALING_EXTRA_KEY_LINE_BUSY);
                if (lineBusy instanceof String) {
                    signallingData.setLineBusy((String) lineBusy);
                } else {
                    TRTCLogger.e(TAG, "lineBusy is not string, value is :" + lineBusy);
                }
            }

            if (extraMap.containsKey(CallModel.SIGNALING_EXTRA_KEY_CALL_END)) {
                Object callEnd = extraMap.get(CallModel.SIGNALING_EXTRA_KEY_CALL_END);
                if (callEnd instanceof Double) {
                    signallingData.setCallEnd(((Double) callEnd).intValue());
                } else {
                    TRTCLogger.e(TAG, "callEnd is not Double, value is :" + callEnd);
                }
            }

            if (extraMap.containsKey(CallModel.SIGNALING_EXTRA_KEY_SWITCH_AUDIO_CALL)) {
                Object switchToAudioCall = extraMap.get(CallModel.SIGNALING_EXTRA_KEY_SWITCH_AUDIO_CALL);
                if (switchToAudioCall instanceof String) {
                    signallingData.setSwitchToAudioCall((String) switchToAudioCall);
                } else {
                    TRTCLogger.e(TAG, "switchToAudioCall is not string, value is :" + switchToAudioCall);
                }
            }

            if (extraMap.containsKey(CallModel.KEY_DATA)) {
                Object dataMapObj = extraMap.get(CallModel.KEY_DATA);
                if (dataMapObj != null && dataMapObj instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) dataMapObj;
                    SignallingData.DataInfo dataInfo = convert2DataInfo(dataMap);
                    signallingData.setData(dataInfo);
                } else {
                    TRTCLogger.e(TAG, "dataMapObj is not map, value is :" + dataMapObj);
                }
            }
        } catch (JsonSyntaxException e) {
            TRTCLogger.e(TAG, "convert2CallingDataBean json parse error");
        }
        return signallingData;
    }

    private void processInvite(String inviteID, String inviter, String groupID, List<String> inviteeList, SignallingData signallingData) {
        CallModel callModel = new CallModel();
        callModel.callId = inviteID;
        callModel.groupId = groupID;
        callModel.action = CallModel.VIDEO_CALL_ACTION_DIALING;
        callModel.invitedList = inviteeList;
        if (isNewSignallingVersion(signallingData)) {
            handleNewSignallingInvite(signallingData, callModel, inviter);
        } else {
            handleOldSignallingInvite(signallingData, callModel, inviter);
        }
    }

    private void handleNewSignallingInvite(SignallingData signallingData, CallModel callModel, String inviter) {
        SignallingData.DataInfo dataInfo = signallingData.getData();
        if (dataInfo == null) {
            TRTCLogger.i(TAG, "signallingData dataInfo is null");
            return;
        }
        callModel.roomId = dataInfo.getRoomID();
        if (CallModel.VALUE_CMD_AUDIO_CALL.equals(dataInfo.getCmd())) {
            callModel.callType = TRTCCalling.TYPE_AUDIO_CALL;
            mCurCallType = callModel.callType;
        } else if (CallModel.VALUE_CMD_VIDEO_CALL.equals(dataInfo.getCmd())) {
            callModel.callType = TRTCCalling.TYPE_VIDEO_CALL;
            mCurCallType = callModel.callType;
        }
        if (CallModel.VALUE_CMD_HAND_UP.equals(dataInfo.getCmd())) {
            preExitRoom(null);
            return;
        }
        if (CallModel.VALUE_CMD_SWITCH_TO_AUDIO.equals(dataInfo.getCmd())) {
            handleSwitchToAudio(callModel, inviter);
            return;
        }
        handleDialing(callModel, inviter);
        if (mCurCallID.equals(callModel.callId)) {
            mLastCallModel = (CallModel) callModel.clone();
        }
    }

    private void handleOldSignallingInvite(SignallingData signallingData, CallModel callModel, String inviter) {
        callModel.callType = signallingData.getCallType();
        mCurCallType = callModel.callType;
        callModel.roomId = signallingData.getRoomId();
        if (signallingData.getCallEnd() != 0) {
            preExitRoom(null);
            return;
        }
        if (CallModel.SIGNALING_EXTRA_KEY_SWITCH_AUDIO_CALL.equals(signallingData.getSwitchToAudioCall())) {
            handleSwitchToAudio(callModel, inviter);
            return;
        }
        handleDialing(callModel, inviter);
        if (mCurCallID.equals(callModel.callId)) {
            mLastCallModel = (CallModel) callModel.clone();
        }
    }

    private SignallingData.DataInfo convert2DataInfo(Map<String, Object> dataMap) {
        SignallingData.DataInfo dataInfo = new SignallingData.DataInfo();
        try {
            if (dataMap.containsKey(CallModel.KEY_CMD)) {
                Object cmd = dataMap.get(CallModel.KEY_CMD);
                if (cmd instanceof String) {
                    dataInfo.setCmd((String) cmd);
                } else {
                    TRTCLogger.e(TAG, "cmd is not string, value is :" + cmd);
                }
            }
            if (dataMap.containsKey(CallModel.KEY_ROOM_ID)) {
                Object roomId = dataMap.get(CallModel.KEY_ROOM_ID);
                if (roomId instanceof Double) {
                    dataInfo.setRoomID(((Double) roomId).intValue());
                } else {
                    TRTCLogger.e(TAG, "roomId is not Double, value is :" + roomId);
                }
            }
            if (dataMap.containsKey(CallModel.KEY_MESSAGE)) {
                Object message = dataMap.get(CallModel.KEY_MESSAGE);
                if (message instanceof String) {
                    dataInfo.setMessage((String) message);
                } else {
                    TRTCLogger.e(TAG, "message is not string, value is :" + message);
                }
            }
        } catch (JsonSyntaxException e) {
            TRTCLogger.e(TAG, "onReceiveNewInvitation JsonSyntaxException:" + e);
        }
        return dataInfo;
    }

    /**
     * TRTC的监听器
     */
    private TRTCCloudListener mTRTCCloudListener = new TRTCCloudListener() {
        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {
            TRTCLogger.e(TAG, "onError: " + errCode + " " + errMsg);
            stopCall();
            if (mTRTCInternalListenerManager != null) {
                mTRTCInternalListenerManager.onError(errCode, errMsg);
            }
        }

        @Override
        public void onEnterRoom(long result) {
            TRTCLogger.d(TAG, "onEnterRoom result:" + result);
            if (result < 0) {
                stopCall();
            } else {
                mIsInRoom = true;
            }
        }

        @Override
        public void onExitRoom(int reason) {
            TRTCLogger.d(TAG, "onExitRoom reason:" + reason);
        }

        @Override
        public void onRemoteUserEnterRoom(String userId) {
            TRTCLogger.d(TAG, "onRemoteUserEnterRoom userId:" + userId);
            mCurRoomRemoteUserSet.add(userId);
            // 只有单聊这个时间才是正确的，因为单聊只会有一个用户进群，群聊这个时间会被后面的人重置
            mEnterRoomTime = System.currentTimeMillis();
            if (mTRTCInternalListenerManager != null) {
                mTRTCInternalListenerManager.onUserEnter(userId);
            }
            if (!mIsBeingCalled) {
                stopDialingMusic();
            }
        }

        @Override
        public void onRemoteUserLeaveRoom(String userId, int reason) {
            TRTCLogger.d(TAG, "onRemoteUserLeaveRoom userId:" + userId + ", reason:" + reason);
            mCurRoomRemoteUserSet.remove(userId);
            mCurInvitedList.remove(userId);
            // 远端用户退出房间，需要判断本次通话是否结束
            if (mTRTCInternalListenerManager != null) {
                mTRTCInternalListenerManager.onUserLeave(userId);
            }
            preExitRoom(userId);
        }

        @Override
        public void onUserVideoAvailable(String userId, boolean available) {
            TRTCLogger.d(TAG, "onUserVideoAvailable userId:" + userId + ", available:" + available);
            if (mTRTCInternalListenerManager != null) {
                mTRTCInternalListenerManager.onUserVideoAvailable(userId, available);
            }
        }

        @Override
        public void onUserAudioAvailable(String userId, boolean available) {
            TRTCLogger.d(TAG, "onUserAudioAvailable userId:" + userId + ", available:" + available);
            if (mTRTCInternalListenerManager != null) {
                mTRTCInternalListenerManager.onUserAudioAvailable(userId, available);
            }
        }

        @Override
        public void onUserVoiceVolume(ArrayList<TRTCCloudDef.TRTCVolumeInfo> userVolumes, int totalVolume) {
            Map<String, Integer> volumeMaps = new HashMap<>();
            for (TRTCCloudDef.TRTCVolumeInfo info : userVolumes) {
                String userId = "";
                if (info.userId == null) {
                    userId = mCurUserId;
                } else {
                    userId = info.userId;
                }
                volumeMaps.put(userId, info.volume);
            }
            mTRTCInternalListenerManager.onUserVoiceVolume(volumeMaps);
        }

        @Override
        public void onNetworkQuality(TRTCCloudDef.TRTCQuality trtcQuality, ArrayList<TRTCCloudDef.TRTCQuality> arrayList) {
            if (mTRTCInternalListenerManager != null) {
                mTRTCInternalListenerManager.onNetworkQuality(trtcQuality, arrayList);
            }
        }
    };


    public TRTCCallingImpl(Context context) {
        mContext = context;
        mTRTCCloud = TRTCCloud.sharedInstance(context);
        mTRTCInternalListenerManager = new TRTCInternalListenerManager();
        mLastCallModel.version = CallModel.VALUE_VERSION;
    }

    private void startCall() {
        mMediaPlayHelper = new MediaPlayHelper(mContext);
        isOnCalling = true;
        UserModelManager.getInstance().getUserModel().userType = UserModel.UserType.CALLING;
        registerSensorEventListener();
    }

    /**
     * 停止此次通话，把所有的变量都会重置
     */
    public void stopCall() {
        isOnCalling = false;
        mIsInRoom = false;
        mEnterRoomTime = 0;
        mCurCallID = "";
        mCurRoomID = 0;
        mCurInvitedList.clear();
        mCurRoomRemoteUserSet.clear();
        mCurSponsorForMe = "";
        mLastCallModel = new CallModel();
        mLastCallModel.version = CallModel.VALUE_VERSION;
        mCurGroupId = "";
        mCurCallType = TYPE_UNKNOWN;
        UserModelManager.getInstance().getUserModel().userType = UserModel.UserType.NONE;
        stopDialingMusic();
        stopRing();
        unregisterSensorEventListener();
    }

    private void realSwitchToAudioCall() {
        if (mCurCallType == TRTCCalling.TYPE_VIDEO_CALL) {
            closeCamera();
            onSwitchToAudio(true, "success");
            mCurCallType = TRTCCalling.TYPE_AUDIO_CALL;
        }
    }

    /**
     * 这里会初始化IM，如果您的项目中已经使用了腾讯云IM，可以删除，不需要再次初始化
     */
    private void initIM() {
        V2TIMSDKConfig config = new V2TIMSDKConfig();
        config.setLogLevel(V2TIMSDKConfig.V2TIM_LOG_DEBUG);
        //初始化 SDK
        mIsInitIMSDK = V2TIMManager.getInstance().initSDK(mContext.getApplicationContext(), mSdkAppId, config, new V2TIMSDKListener() {
            @Override
            public void onConnecting() {
            }

            @Override
            public void onConnectSuccess() {
            }

            @Override
            public void onConnectFailed(int code, String error) {
                TRTCLogger.e(TAG, "init im sdk error.");
            }
        });
    }

    /**
     * 判断用户是否正忙
     *
     * @param userType
     * @return
     */
    private boolean isUserModelBusy(UserModel.UserType userType) {
        return userType != UserModel.UserType.NONE;
    }

    public void handleDialing(CallModel callModel, String user) {
        //正在体验视频互动、语聊房、语音沙龙模块时，收到一个邀请我的通话请求，告诉对方忙线
        UserModel.UserType userType = UserModelManager.getInstance().getUserModel().userType;
        if (isUserModelBusy(userType)) {
            sendModel(user, CallModel.VIDEO_CALL_ACTION_LINE_BUSY, callModel, null);
            return;
        }

        if (!TextUtils.isEmpty(mCurCallID)) {
            // 正在通话时，收到了一个邀请我的通话请求,需要告诉对方忙线
            if (isOnCalling && callModel.invitedList.contains(mCurUserId)) {
                sendModel(user, CallModel.VIDEO_CALL_ACTION_LINE_BUSY, callModel, null);
                return;
            }
            // 与对方处在同一个群中，此时收到了邀请群中其他人通话的请求，界面上展示连接动画
            if (!TextUtils.isEmpty(mCurGroupId) && !TextUtils.isEmpty(callModel.groupId)) {
                if (mCurGroupId.equals(callModel.groupId)) {
                    mCurInvitedList.addAll(callModel.invitedList);
                    if (mTRTCInternalListenerManager != null) {
                        mTRTCInternalListenerManager.onGroupCallInviteeListUpdate(mCurInvitedList);
                    }
                    return;
                }
            }
        }

        // 虽然是群组聊天，但是对方并没有邀请我，我不做处理
        if (!TextUtils.isEmpty(callModel.groupId) && !callModel.invitedList.contains(mCurUserId)) {
            return;
        }

        // 开始接通电话
        startCall();
        mCurCallID = callModel.callId;
        mCurRoomID = callModel.roomId;
        mCurCallType = callModel.callType;
        mCurSponsorForMe = user;
        mCurGroupId = callModel.groupId;
        // 邀请列表中需要移除掉自己
        callModel.invitedList.remove(mCurUserId);
        List<String> onInvitedUserListParam = callModel.invitedList;
        if (!TextUtils.isEmpty(mCurGroupId)) {
            mCurInvitedList.addAll(callModel.invitedList);
        }
        if (mTRTCInternalListenerManager != null) {
            mTRTCInternalListenerManager.onInvited(user, onInvitedUserListParam, !TextUtils.isEmpty(mCurGroupId), mCurCallType);
        }
        mCurRoomRemoteUserSet.add(user);
        startRing();
    }

    private void handleSwitchToAudio(CallModel callModel, String user) {
        if (mCurCallType != TRTCCalling.TYPE_VIDEO_CALL) {
            sendModel(user, CallModel.VIDEO_CALL_ACTION_REJECT_SWITCH_TO_AUDIO, callModel, "reject, remote user call type is not video call");
            return;
        }
        sendModel(user, CallModel.VIDEO_CALL_ACTION_ACCEPT_SWITCH_TO_AUDIO, callModel, "");
    }

    @Override
    public void destroy() {
        //必要的清除逻辑
        V2TIMManager.getSignalingManager().removeSignalingListener(mTIMSignallingListener);
        mTRTCCloud.stopLocalPreview();
        mTRTCCloud.stopLocalAudio();
        mTRTCCloud.exitRoom();
    }

    @Override
    public void addDelegate(TRTCCallingDelegate delegate) {
        mTRTCInternalListenerManager.addDelegate(delegate);
    }

    @Override
    public void removeDelegate(TRTCCallingDelegate delegate) {
        mTRTCInternalListenerManager.removeDelegate(delegate);
    }

    @Override
    public void login(int sdkAppId, final String userId, final String userSig, final TRTCCalling.ActionCallBack callback) {
        TRTCLogger.i(TAG, "start login, sdkAppId:" + sdkAppId + " userId:" + userId + " sign is empty:" + TextUtils.isEmpty(userSig));
        if (sdkAppId == 0 || TextUtils.isEmpty(userId) || TextUtils.isEmpty(userSig)) {
            TRTCLogger.e(TAG, "start login fail. params invalid.");
            if (callback != null) {
                callback.onError(-1, "login fail, params is invalid.");
            }
            return;
        }
        mSdkAppId = sdkAppId;
        //1. 未初始化 IM 先初始化 IM
        if (!mIsInitIMSDK) {
            initIM();
        }
        //2. 需要将监听器添加到IM上
        V2TIMManager.getSignalingManager().addSignalingListener(mTIMSignallingListener);
        V2TIMManager.getMessageManager();

        String loginUser = V2TIMManager.getInstance().getLoginUser();
        if (loginUser != null && loginUser.equals(userId)) {
            TRTCLogger.d(TAG, "IM already login user：" + loginUser);
            mCurUserId = loginUser;
            mCurUserSig = userSig;
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }

        V2TIMManager.getInstance().login(userId, userSig, new V2TIMCallback() {
            @Override
            public void onError(int i, String s) {
                if (callback != null) {
                    callback.onError(i, s);
                }
            }

            @Override
            public void onSuccess() {
                mCurUserId = userId;
                mCurUserSig = userSig;
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    @Override
    public void logout(final TRTCCalling.ActionCallBack callBack) {
        V2TIMManager.getInstance().logout(new V2TIMCallback() {
            @Override
            public void onError(int i, String s) {
                if (callBack != null) {
                    callBack.onError(i, s);
                }
            }

            @Override
            public void onSuccess() {
                if (callBack != null) {
                    callBack.onSuccess();
                }
            }
        });
        stopCall();
        exitRoom();
        mNickName = "";
        mFaceUrl = "";
    }

    @Override
    public void call(final String userId, int type) {
        TRTCLogger.i(TAG, "start single call " + userId + ", type " + type);
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        List<String> list = new ArrayList<>();
        list.add(userId);
        internalCall(list, type, "");
    }

    @Override
    public void groupCall(final List<String> userIdList, int type, String groupId) {
        if (isCollectionEmpty(userIdList)) {
            return;
        }
        internalCall(userIdList, type, groupId);
    }

    /**
     * 统一的拨打逻辑
     *
     * @param userIdList 需要邀请的用户列表
     * @param type       邀请类型
     * @param groupId    群组通话的group id，如果是C2C需要传 ""
     */
    private void internalCall(final List<String> userIdList, int type, String groupId) {
        final boolean isGroupCall = !TextUtils.isEmpty(groupId);
        if (!isOnCalling) {
            // 首次拨打电话，生成id，并进入trtc房间
            mCurRoomID = generateRoomID();
            mCurGroupId = groupId;
            mCurCallType = type;
            TRTCLogger.d(TAG, "First calling, generate room id " + mCurRoomID);
            enterTRTCRoom();
            startCall();
            mIsBeingCalled = false;
            startDialingMusic();
        }
        // 非首次拨打，不能发起新的groupId通话
        if (!TextUtils.equals(mCurGroupId, groupId)) {
            return;
        }

        // 过滤已经邀请的用户id
        List<String> filterInvitedList = new ArrayList<>();
        for (String id : userIdList) {
            if (!mCurInvitedList.contains(id)) {
                filterInvitedList.add(id);
            }
        }
        // 如果当前没有需要邀请的id则返回
        if (isCollectionEmpty(filterInvitedList)) {
            return;
        }

        mCurInvitedList.addAll(filterInvitedList);
        mCurRoomRemoteUserSet.addAll(filterInvitedList);
        TRTCLogger.i(TAG, "groupCall: filter:" + filterInvitedList + " all:" + mCurInvitedList);
        // 填充通话信令的model
        mLastCallModel.action = CallModel.VIDEO_CALL_ACTION_DIALING;
        mLastCallModel.invitedList = mCurInvitedList;
        mLastCallModel.roomId = mCurRoomID;
        mLastCallModel.groupId = mCurGroupId;
        mLastCallModel.callType = mCurCallType;

        // 首次拨打电话，生成id
        if (!TextUtils.isEmpty(mCurGroupId)) {
            // 群聊发送群消息
            mCurCallID = sendModel("", CallModel.VIDEO_CALL_ACTION_DIALING);
        } else {
            // 单聊发送C2C消息
            for (final String userId : filterInvitedList) {
                mCurCallID = sendModel(userId, CallModel.VIDEO_CALL_ACTION_DIALING);
            }
        }
        mLastCallModel.callId = mCurCallID;
    }

    /**
     * 重要：用于判断是否需要结束本次通话
     * 在用户超时、拒绝、忙线、有人退出房间时需要进行判断
     */
    private void preExitRoom(String leaveUser) {
        TRTCLogger.i(TAG, "preExitRoom: " + mCurRoomRemoteUserSet + " " + mCurInvitedList);
        if (mCurRoomRemoteUserSet.isEmpty() && mCurInvitedList.isEmpty() && mIsInRoom) {
            // 当没有其他用户在房间里了，则结束通话。
            if (!TextUtils.isEmpty(leaveUser)) {
                if (TextUtils.isEmpty(mCurGroupId)) {
                    sendModel(leaveUser, CallModel.VIDEO_CALL_ACTION_HANGUP);
                } else {
                    sendModel("", CallModel.VIDEO_CALL_ACTION_HANGUP);
                }
            }
            playHangupMusic();
            exitRoom();
            stopCall();
            if (mTRTCInternalListenerManager != null) {
                mTRTCInternalListenerManager.onCallEnd();
            }
        }
    }

    /**
     * trtc 退房
     */
    private void exitRoom() {
        mTRTCCloud.stopLocalPreview();
        mTRTCCloud.stopLocalAudio();
        mTRTCCloud.exitRoom();
    }

    @Override
    public void accept() {
        sendModel(mCurSponsorForMe, CallModel.VIDEO_CALL_ACTION_ACCEPT);
        enterTRTCRoom();
        stopRing();
    }

    /**
     * trtc 进房
     */
    private void enterTRTCRoom() {
        if (mCurCallType == TRTCCalling.TYPE_VIDEO_CALL) {
            // 开启基础美颜
            TXBeautyManager txBeautyManager = mTRTCCloud.getBeautyManager();
            // 自然美颜
            txBeautyManager.setBeautyStyle(1);
            txBeautyManager.setBeautyLevel(6);
            // 进房前需要设置一下关键参数
            TRTCCloudDef.TRTCVideoEncParam encParam = new TRTCCloudDef.TRTCVideoEncParam();
            encParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_960_540;
            encParam.videoFps = 15;
            encParam.videoBitrate = 1000;
            encParam.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT;
            encParam.enableAdjustRes = true;
            mTRTCCloud.setVideoEncoderParam(encParam);
        }
        TRTCLogger.i(TAG, "enterTRTCRoom: " + mCurUserId + " room:" + mCurRoomID);
        TRTCCloudDef.TRTCParams TRTCParams = new TRTCCloudDef.TRTCParams(mSdkAppId, mCurUserId, mCurUserSig, mCurRoomID, "", "");
        TRTCParams.role = TRTCCloudDef.TRTCRoleAnchor;
        mTRTCCloud.enableAudioVolumeEvaluation(300);
        mTRTCCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER);
        mTRTCCloud.startLocalAudio();
        // 收到来电，开始监听 trtc 的消息
        mTRTCCloud.setListener(mTRTCCloudListener);
        mTRTCCloud.enterRoom(TRTCParams, mCurCallType == TRTCCalling.TYPE_VIDEO_CALL ? TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL : TRTCCloudDef.TRTC_APP_SCENE_AUDIOCALL);
    }

    @Override
    public void reject() {
        playHangupMusic();
        sendModel(mCurSponsorForMe, CallModel.VIDEO_CALL_ACTION_REJECT);
        stopCall();
    }

    @Override
    public void hangup() {
        // 1. 如果还没有在通话中，说明还没有接通，所以直接拒绝了
        if (!isOnCalling) {
            reject();
            return;
        }
        playHangupMusic();
        boolean fromGroup = (!TextUtils.isEmpty(mCurGroupId));
        if (fromGroup) {
            TRTCLogger.d(TAG, "groupHangup");
            groupHangup();
        } else {
            TRTCLogger.d(TAG, "singleHangup");
            singleHangup();
        }
    }

    private void groupHangup() {
        if (isCollectionEmpty(mCurRoomRemoteUserSet)) {
            // 当前以及没有人在通话了，直接向群里发送一个取消消息
            sendModel("", CallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL);
        }
        stopCall();
        exitRoom();
    }

    private void singleHangup() {
        for (String id : mCurInvitedList) {
            sendModel(id, CallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL);
        }
        stopCall();
        exitRoom();
    }

    @Override
    public void openCamera(boolean isFrontCamera, TXCloudVideoView txCloudVideoView) {
        if (txCloudVideoView == null) {
            return;
        }
        mIsUseFrontCamera = isFrontCamera;
        mTRTCCloud.startLocalPreview(isFrontCamera, txCloudVideoView);
    }

    @Override
    public void closeCamera() {
        mTRTCCloud.stopLocalPreview();
    }

    @Override
    public void startRemoteView(String userId, TXCloudVideoView txCloudVideoView) {
        if (txCloudVideoView == null) {
            return;
        }
        mTRTCCloud.startRemoteView(userId, txCloudVideoView);
    }

    @Override
    public void stopRemoteView(String userId) {
        mTRTCCloud.stopRemoteView(userId);
    }

    @Override
    public void switchCamera(boolean isFrontCamera) {
        if (mIsUseFrontCamera == isFrontCamera) {
            return;
        }
        mIsUseFrontCamera = isFrontCamera;
        mTRTCCloud.switchCamera();
    }

    @Override
    public void setMicMute(boolean isMute) {
        mTRTCCloud.muteLocalAudio(isMute);
    }

    @Override
    public void setHandsFree(boolean isHandsFree) {
        if (isHandsFree) {
            mTRTCCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER);
        } else {
            mTRTCCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_EARPIECE);
        }
    }

    @Override
    public void switchToAudioCall() {
        if (mCurCallType != TRTCCalling.TYPE_VIDEO_CALL) {
            onSwitchToAudio(false, "the call type of current user is not video call");
            return;
        }
        if (mCurRoomRemoteUserSet.isEmpty()) {
            onSwitchToAudio(false, "remote user is empty");
            return;
        }
        for (final String userId : mCurRoomRemoteUserSet) {
            mCurCallID = sendModel(userId, CallModel.VIDEO_CALL_SWITCH_TO_AUDIO_CALL);
        }
    }

    @Override
    public void setSelfProfile(String userName, String avatarURL, final TRTCCallingCallback.ActionCallback callback) {
        V2TIMUserFullInfo v2TIMUserFullInfo = new V2TIMUserFullInfo();
        v2TIMUserFullInfo.setNickname(userName);
        v2TIMUserFullInfo.setFaceUrl(avatarURL);
        V2TIMManager.getInstance().setSelfInfo(v2TIMUserFullInfo, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                TRTCLogger.e(TAG, "set profile code:" + code + " msg:" + desc);
                if (callback != null) {
                    callback.onCallback(code, desc);
                }
            }

            @Override
            public void onSuccess() {
                TRTCLogger.i(TAG, "set profile success.");
                if (callback != null) {
                    callback.onCallback(0, "set profile success.");
                }
            }
        });
    }

    private String sendModel(final String user, int action) {
        return sendModel(user, action, null, null);
    }

    private void sendOnlineMessageWithOfflinePushInfo(final String user, final CallModel model) {
        List<String> users = new ArrayList<String>();
        users.add(V2TIMManager.getInstance().getLoginUser());
        if (!TextUtils.isEmpty(mNickName)) {
            sendOnlineMessageWithOfflinePushInfo(user, mNickName, model);
            return;
        }
        V2TIMManager.getInstance().getUsersInfo(users, new V2TIMValueCallback<List<V2TIMUserFullInfo>>() {

            @Override
            public void onError(int code, String desc) {
                TRTCLogger.e(TAG, "getUsersInfo err code = " + code + ", desc = " + desc);
                sendOnlineMessageWithOfflinePushInfo(user, null, model);
            }

            @Override
            public void onSuccess(List<V2TIMUserFullInfo> v2TIMUserFullInfos) {
                if (v2TIMUserFullInfos == null || v2TIMUserFullInfos.size() == 0) {
                    sendOnlineMessageWithOfflinePushInfo(user, null, model);
                    return;
                }
                mNickName = v2TIMUserFullInfos.get(0).getNickName();
                mFaceUrl = v2TIMUserFullInfos.get(0).getFaceUrl();
                sendOnlineMessageWithOfflinePushInfo(user, v2TIMUserFullInfos.get(0).getNickName(), model);
            }
        });
    }

    private String getCallId() {
        return mCurCallID;
    }

    private void sendOnlineMessageWithOfflinePushInfo(String userId, String nickname, CallModel model) {
        OfflineMessageContainerBean containerBean = new OfflineMessageContainerBean();
        OfflineMessageBean entity = new OfflineMessageBean();
        entity.content = new Gson().toJson(model);
        entity.sender = V2TIMManager.getInstance().getLoginUser(); // 发送者肯定是登录账号
        entity.action = OfflineMessageBean.REDIRECT_ACTION_CALL;
        entity.sendTime = System.currentTimeMillis() / 1000;
        entity.nickname = nickname;
        entity.faceUrl = mFaceUrl;
        containerBean.entity = entity;
        List<String> invitedList = new ArrayList<>();
        final boolean isGroup = (!TextUtils.isEmpty(model.groupId));
        if (isGroup) {
            entity.chatType = V2TIMConversation.V2TIM_GROUP;
            invitedList.addAll(model.invitedList);
        } else {
            invitedList.add(userId);
        }

        V2TIMOfflinePushInfo v2TIMOfflinePushInfo = new V2TIMOfflinePushInfo();
        v2TIMOfflinePushInfo.setExt(new Gson().toJson(containerBean).getBytes());
        // OPPO必须设置ChannelID才可以收到推送消息，这个channelID需要和控制台一致
        v2TIMOfflinePushInfo.setAndroidOPPOChannelID("tuikit");
        v2TIMOfflinePushInfo.setDesc(mContext.getString(R.string.trtccalling_title_have_a_call_invitation));
        v2TIMOfflinePushInfo.setTitle(nickname);
        MessageCustom custom = new MessageCustom();
        custom.businessID = MessageCustom.BUSINESS_ID_AV_CALL;
        V2TIMMessage message = V2TIMManager.getMessageManager().createCustomMessage(new Gson().toJson(custom).getBytes());

        for (String receiver : invitedList) {
            TRTCLogger.i(TAG, "sendOnlineMessage to " + receiver);
            V2TIMManager.getMessageManager().sendMessage(message, receiver, null, V2TIMMessage.V2TIM_PRIORITY_DEFAULT,
                    true, v2TIMOfflinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {

                        @Override
                        public void onError(int code, String desc) {
                            TRTCLogger.e(TAG, "sendOnlineMessage failed, code:" + code + ", desc:" + desc);
                        }

                        @Override
                        public void onSuccess(V2TIMMessage v2TIMMessage) {
                            TRTCLogger.i(TAG, "sendOnlineMessage msgId:" + v2TIMMessage.getMsgID());
                        }

                        @Override
                        public void onProgress(int progress) {

                        }
                    });
        }
    }

    /**
     * 信令发送函数，当CallModel 存在groupId时会向群组发送信令
     *
     * @param user
     * @param action
     * @param model
     * @param message
     */
    private String sendModel(final String user, int action, CallModel model, String message) {
        String callID = null;
        final CallModel realCallModel;
        if (model != null) {
            realCallModel = (CallModel) model.clone();
            realCallModel.action = action;
        } else {
            realCallModel = generateModel(action);
        }
        final boolean isGroup = (!TextUtils.isEmpty(realCallModel.groupId));
        if (action == CallModel.VIDEO_CALL_ACTION_HANGUP && mEnterRoomTime != 0 && !isGroup) {
            realCallModel.duration = (int) (System.currentTimeMillis() - mEnterRoomTime) / 1000;
            mEnterRoomTime = 0;
        }
        String receiver = "";
        String groupId = "";
        if (isGroup) {
            groupId = realCallModel.groupId;
        } else {
            receiver = user;
        }
        final SignallingData signallingData = createSignallingData();
        signallingData.setCallType(realCallModel.callType);
        GsonBuilder gsonBuilder = new GsonBuilder();
        // signalling
        switch (realCallModel.action) {
            case CallModel.VIDEO_CALL_ACTION_DIALING:
                signallingData.setRoomId(realCallModel.roomId);
                SignallingData.DataInfo callDataInfo = new SignallingData.DataInfo();
                if (realCallModel.callType == TRTCCalling.TYPE_AUDIO_CALL) {
                    callDataInfo.setCmd(CallModel.VALUE_CMD_AUDIO_CALL);
                } else if (realCallModel.callType == TRTCCalling.TYPE_VIDEO_CALL) {
                    callDataInfo.setCmd(CallModel.VALUE_CMD_VIDEO_CALL);
                } else {
                    break;
                }
                callDataInfo.setRoomID(realCallModel.roomId);
                signallingData.setData(callDataInfo);
                addFilterKey(gsonBuilder, CallModel.SIGNALING_EXTRA_KEY_CALL_END);
                String dialingDataStr = gsonBuilder.create().toJson(signallingData);
                if (isGroup) {
                    callID = sendGroupInvite(groupId, realCallModel.invitedList, dialingDataStr, TIME_OUT_COUNT, new V2TIMCallback() {
                        @Override
                        public void onError(int code, String desc) {
                            TRTCLogger.e(TAG, "inviteInGroup callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc);
                        }

                        @Override
                        public void onSuccess() {
                            TRTCLogger.d(TAG, "inviteInGroup success:" + realCallModel);
                            realCallModel.callId = getCallId();
                            realCallModel.timeout = TIME_OUT_COUNT;
                            realCallModel.version = CallModel.VALUE_VERSION;
                            sendOnlineMessageWithOfflinePushInfo(user, realCallModel);
                        }
                    });
                } else {
                    callID = sendInvite(receiver, dialingDataStr, TIME_OUT_COUNT, new V2TIMCallback() {
                        @Override
                        public void onError(int code, String desc) {
                            TRTCLogger.e(TAG, "invite  callID:" + realCallModel.callId + ",error:" + code + " desc:" + desc);
                        }

                        @Override
                        public void onSuccess() {
                            TRTCLogger.d(TAG, "invite success:" + realCallModel);
                            realCallModel.callId = getCallId();
                            realCallModel.timeout = TIME_OUT_COUNT;
                            realCallModel.version = CallModel.VALUE_VERSION;
                            sendOnlineMessageWithOfflinePushInfo(user, realCallModel);
                        }
                    });
                }
                break;
            case CallModel.VIDEO_CALL_ACTION_ACCEPT:
                addFilterKey(gsonBuilder, CallModel.SIGNALING_EXTRA_KEY_CALL_END);
                String acceptDataStr = gsonBuilder.create().toJson(signallingData);
                acceptInvite(realCallModel.callId, acceptDataStr, new V2TIMCallback() {
                    @Override
                    public void onError(int code, String desc) {
                        TRTCLogger.e(TAG, "accept callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc);
                    }

                    @Override
                    public void onSuccess() {
                        TRTCLogger.d(TAG, "accept success callID:" + realCallModel.callId);
                    }
                });
                break;

            case CallModel.VIDEO_CALL_ACTION_REJECT:
                addFilterKey(gsonBuilder, CallModel.SIGNALING_EXTRA_KEY_CALL_END);
                String rejectDataStr = gsonBuilder.create().toJson(signallingData);
                rejectInvite(realCallModel.callId, rejectDataStr, new V2TIMCallback() {
                    @Override
                    public void onError(int code, String desc) {
                        TRTCLogger.e(TAG, "reject callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc);
                    }

                    @Override
                    public void onSuccess() {
                        TRTCLogger.d(TAG, "reject success callID:" + realCallModel.callId);
                    }
                });
                break;
            case CallModel.VIDEO_CALL_ACTION_LINE_BUSY:
                addFilterKey(gsonBuilder, CallModel.SIGNALING_EXTRA_KEY_CALL_END);
                SignallingData.DataInfo lineBusyDataInfo = new SignallingData.DataInfo();
                signallingData.setLineBusy(CallModel.SIGNALING_EXTRA_KEY_LINE_BUSY);
                lineBusyDataInfo.setMessage(CallModel.VALUE_MSG_LINE_BUSY);
                signallingData.setData(lineBusyDataInfo);
                String lineBusyMapStr = new Gson().toJson(signallingData);
                rejectInvite(realCallModel.callId, lineBusyMapStr, new V2TIMCallback() {
                    @Override
                    public void onError(int code, String desc) {
                        TRTCLogger.e(TAG, "reject  callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc);
                    }

                    @Override
                    public void onSuccess() {
                        TRTCLogger.d(TAG, "reject success callID:" + realCallModel.callId);
                    }
                });
                break;

            case CallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL:
                addFilterKey(gsonBuilder, CallModel.SIGNALING_EXTRA_KEY_CALL_END);
                String cancelMapStr = gsonBuilder.create().toJson(signallingData);
                cancelInvite(realCallModel.callId, cancelMapStr, new V2TIMCallback() {
                    @Override
                    public void onError(int code, String desc) {
                        TRTCLogger.e(TAG, "cancel callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc);
                    }

                    @Override
                    public void onSuccess() {
                        TRTCLogger.d(TAG, "cancel success callID:" + realCallModel.callId);
                    }
                });
                break;

            case CallModel.VIDEO_CALL_ACTION_HANGUP:
                SignallingData.DataInfo hangupDataInfo = new SignallingData.DataInfo();
                signallingData.setCallEnd(realCallModel.duration);
                hangupDataInfo.setCmd(CallModel.VALUE_CMD_HAND_UP);
                signallingData.setData(hangupDataInfo);
                String hangupMapStr = gsonBuilder.create().toJson(signallingData);
                if (isGroup) {
                    sendGroupInvite(groupId, realCallModel.invitedList, hangupMapStr, 0, new V2TIMCallback() {
                        @Override
                        public void onError(int code, String desc) {
                            TRTCLogger.e(TAG, "inviteInGroup-->hangup callID: " + realCallModel.callId + ", error:" + code + " desc:" + desc);
                        }

                        @Override
                        public void onSuccess() {
                            TRTCLogger.d(TAG, "inviteInGroup-->hangup success callID:" + realCallModel.callId);
                        }
                    });
                } else {
                    sendInvite(receiver, hangupMapStr, TIME_OUT_COUNT, new V2TIMCallback() {
                        @Override
                        public void onError(int code, String desc) {
                            TRTCLogger.e(TAG, "invite-->hangup callID: " + realCallModel.callId + ", error:" + code + " desc:" + desc);
                        }

                        @Override
                        public void onSuccess() {
                            TRTCLogger.d(TAG, "invite-->hangup success callID:" + realCallModel.callId);
                        }
                    });
                }
                break;

            case CallModel.VIDEO_CALL_SWITCH_TO_AUDIO_CALL:
                addFilterKey(gsonBuilder, CallModel.SIGNALING_EXTRA_KEY_CALL_END);
                SignallingData.DataInfo switchAudioCallDataInfo = new SignallingData.DataInfo();
                switchAudioCallDataInfo.setCmd(CallModel.VALUE_CMD_SWITCH_TO_AUDIO);
                signallingData.setSwitchToAudioCall(CallModel.VALUE_CMD_SWITCH_TO_AUDIO);
                signallingData.setData(switchAudioCallDataInfo);
                String switchAudioCall = gsonBuilder.create().toJson(signallingData);
                sendInvite(receiver, switchAudioCall, TIME_OUT_COUNT, new V2TIMCallback() {
                    @Override
                    public void onError(int code, String desc) {
                        TRTCLogger.e(TAG, "invite-->switchAudioCall callID: " + realCallModel.callId + ", error:" + code + " desc:" + desc);
                    }

                    @Override
                    public void onSuccess() {
                        TRTCLogger.d(TAG, "invite-->switchAudioCall success callID:" + realCallModel.callId);
                    }
                });
                break;

            case CallModel.VIDEO_CALL_ACTION_ACCEPT_SWITCH_TO_AUDIO:
                addFilterKey(gsonBuilder, CallModel.SIGNALING_EXTRA_KEY_CALL_END);
                SignallingData.DataInfo acceptSwitchAudioCallData = new SignallingData.DataInfo();
                acceptSwitchAudioCallData.setCmd(CallModel.VALUE_CMD_SWITCH_TO_AUDIO);
                signallingData.setSwitchToAudioCall(CallModel.VALUE_CMD_SWITCH_TO_AUDIO);
                signallingData.setData(acceptSwitchAudioCallData);
                String acceptSwitchAudioDataStr = gsonBuilder.create().toJson(signallingData);
                acceptInvite(realCallModel.callId, acceptSwitchAudioDataStr, new V2TIMCallback() {
                    @Override
                    public void onError(int code, String desc) {
                        TRTCLogger.e(TAG, "accept switch audio call callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc);
                    }

                    @Override
                    public void onSuccess() {
                        TRTCLogger.d(TAG, "accept switch audio call success callID:" + realCallModel.callId);
                        realSwitchToAudioCall();
                    }
                });
                break;

            case CallModel.VIDEO_CALL_ACTION_REJECT_SWITCH_TO_AUDIO:
                addFilterKey(gsonBuilder, CallModel.SIGNALING_EXTRA_KEY_CALL_END);
                SignallingData.DataInfo rejectSwitchAudioCallData = new SignallingData.DataInfo();
                rejectSwitchAudioCallData.setCmd(CallModel.VALUE_CMD_SWITCH_TO_AUDIO);
                signallingData.setSwitchToAudioCall(CallModel.VALUE_CMD_SWITCH_TO_AUDIO);
                rejectSwitchAudioCallData.setMessage(message);
                signallingData.setData(rejectSwitchAudioCallData);
                String rejectSwitchAudioMapStr = gsonBuilder.create().toJson(signallingData);
                rejectInvite(realCallModel.callId, rejectSwitchAudioMapStr, new V2TIMCallback() {
                    @Override
                    public void onError(int code, String desc) {
                        TRTCLogger.e(TAG, "reject switch to audio callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc);
                    }

                    @Override
                    public void onSuccess() {
                        TRTCLogger.d(TAG, "reject switch to audio success callID:" + realCallModel.callId);
                    }
                });
                break;
            default:
                break;
        }

        // 最后需要重新赋值
        if (realCallModel.action != CallModel.VIDEO_CALL_ACTION_REJECT &&
                realCallModel.action != CallModel.VIDEO_CALL_ACTION_HANGUP &&
                realCallModel.action != CallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL &&
                model == null) {
            mLastCallModel = (CallModel) realCallModel.clone();
        }
        return callID;
    }

    private SignallingData createSignallingData() {
        SignallingData signallingData = new SignallingData();
        signallingData.setVersion(CallModel.VALUE_VERSION);
        signallingData.setBusinessID(CallModel.VALUE_BUSINESS_ID);
        signallingData.setPlatform(CallModel.VALUE_PLATFORM);
        return signallingData;
    }

    private void onSwitchToAudio(boolean success, String message) {
        if (mTRTCInternalListenerManager != null) {
            mTRTCInternalListenerManager.onSwitchToAudio(success, message);
        }
    }

    private CallModel generateModel(int action) {
        CallModel callModel = (CallModel) mLastCallModel.clone();
        callModel.action = action;
        return callModel;
    }

    private static boolean isCollectionEmpty(Collection coll) {
        return coll == null || coll.size() == 0;
    }

    private static int generateRoomID() {
        Random random = new Random();
        return random.nextInt(ROOM_ID_MAX - ROOM_ID_MIN + 1) + ROOM_ID_MIN;
    }

    private String sendInvite(String receiver, String data, int timeout, final V2TIMCallback callback) {
        return V2TIMManager.getSignalingManager().invite(receiver, data, true, null, timeout, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                if (callback != null) {
                    callback.onError(code, desc);
                }
            }

            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    private String sendGroupInvite(String groupId, List<String> inviteeList, String data, int timeout, final V2TIMCallback callback) {
        return V2TIMManager.getSignalingManager().inviteInGroup(groupId, inviteeList, data, true, timeout, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                if (callback != null) {
                    callback.onError(code, desc);
                }
            }

            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    private void acceptInvite(String inviteId, String data, final V2TIMCallback callback) {
        V2TIMManager.getSignalingManager().accept(inviteId, data, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                if (callback != null) {
                    callback.onError(code, desc);
                }
            }

            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    private void rejectInvite(String inviteId, String data, final V2TIMCallback callback) {
        V2TIMManager.getSignalingManager().reject(inviteId, data, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                if (callback != null) {
                    callback.onError(code, desc);
                }
            }

            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    private void cancelInvite(String inviteId, String data, final V2TIMCallback callback) {
        V2TIMManager.getSignalingManager().cancel(inviteId, data, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                if (callback != null) {
                    callback.onError(code, desc);
                }
            }

            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    //在json中过滤不需要的key
    private void addFilterKey(GsonBuilder builder, final String... keys) {
        for (final String key : keys) {
            builder.setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getName().contains(key);
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            });
        }
    }

    private void registerSensorEventListener() {
        if (null != mSensorManager) {
            return;
        }
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "TUICalling:TRTCAudioCallWakeLock");
        mSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_PROXIMITY:
                        // 靠近手机
                        if (event.values[0] == 0.0) {
                            if (wakeLock.isHeld()) {
                                // 检查WakeLock是否被占用
                                return;
                            } else {
                                // 申请设备电源锁
                                wakeLock.acquire();
                            }
                        } else {
                            if (!wakeLock.isHeld()) {
                                return;
                            } else {
                                wakeLock.setReferenceCounted(false);
                                // 释放设备电源锁
                                wakeLock.release();
                            }
                            break;
                        }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        mSensorManager.registerListener(mSensorEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterSensorEventListener() {
        if (null != mSensorManager && null != mSensorEventListener) {
            Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            mSensorManager.unregisterListener(mSensorEventListener, sensor);
            mSensorManager = null;
        }
    }

    private void startDialingMusic() {
        mMediaPlayHelper.start(R.raw.phone_dialing);
    }

    private void stopDialingMusic() {
        stopMusic();
    }

    private void startRing() {
        mMediaPlayHelper.start(R.raw.phone_ringing);
    }

    private void stopRing() {
        stopMusic();
    }

    private void playHangupMusic() {
        mMediaPlayHelper.start(R.raw.phone_hangup, 2000);
    }

    private void stopMusic() {
        final int resId = mMediaPlayHelper.getResId();
        // 挂断音效很短，播放完即可；主叫铃音和被叫铃音需主动stop
        if (resId != R.raw.phone_hangup) {
            mMediaPlayHelper.stop();
        }
    }
}
