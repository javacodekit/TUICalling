//
//  TRTCCallingContactViewController.swift
//  TXLiteAVDemo
//
//  Created by abyyxwang on 2020/8/6.
//  Copyright © 2020 Tencent. All rights reserved.
//

import Foundation
import Toast_Swift
import TUICalling
import ImSDK_Plus

enum CallingUserRemoveReason: UInt32 {
    case leave = 0
    case reject
    case noresp
    case busy
}


public class TRTCCallingContactViewController: UIViewController, TRTCCallingDelegate {
    var selectedFinished: (([V2TIMUserFullInfo])->Void)? = nil
    var callVC: CallingViewControllerResponder? = nil
    @objc var callType: CallType = .audio
    /// 是否展示搜索结果
    var shouldShowSearchResult: Bool = false {
        didSet {
            if oldValue != shouldShowSearchResult {
                selectTable.reloadData()
            }
        }
    }
    
    /// 搜索结果model
    var searchResult: V2TIMUserFullInfo? = nil
    
    let searchContainerView: UIView = {
        let view = UIView.init(frame: .zero)
        view.backgroundColor = .clear
        return view
    }()
    
    let searchBar: UISearchBar = {
        let searchBar = UISearchBar.init()
        searchBar.backgroundImage = UIImage.init()
        searchBar.placeholder = .searchPhoneNumberText
        searchBar.backgroundColor = UIColor(hex: "F4F5F9")
        searchBar.barTintColor = .clear
        searchBar.returnKeyType = .search
        searchBar.layer.cornerRadius = 20
        return searchBar
    }()
    
    /// 搜索按钮
    lazy var searchBtn: UIButton = {
        let done = UIButton(type: .custom)
        done.setTitle(.searchText, for: .normal)
        done.setTitleColor(.white, for: .normal)
        done.titleLabel?.font = UIFont.systemFont(ofSize: 16)
        done.backgroundColor = UIColor(hex: "006EFF")
        done.clipsToBounds = true
        done.layer.cornerRadius = 20
        return done
    }()
    
    let userInfoLabel: UILabel = {
        let label = UILabel.init(frame: .zero)
        //TODO: 此处需要从IM获取到用户手机号
        label.text = "\(String.yourUserNameText):\("\(V2TIMManager.sharedInstance()?.getLoginUser() ?? "")")"
        label.font = UIFont.systemFont(ofSize: 16.0)
        label.textColor = .black
        label.textAlignment = .left
        return label
    }()
    
    /// 选择列表
    lazy var selectTable: UITableView = {
        let table = UITableView(frame: CGRect.zero, style: .plain)
        table.tableFooterView = UIView(frame: .zero)
        table.backgroundColor = .clear
        table.register(CallingSelectUserTableViewCell.classForCoder(), forCellReuseIdentifier: "CallingSelectUserTableViewCell")
        table.delegate = self
        table.dataSource = self
        return table
    }()
    
    let kUserBorder: CGFloat = 44.0
    let kUserSpacing: CGFloat = 2
    let kUserPanelLeftSpacing: CGFloat = 28
    
    /// 搜索记录为空时，提示
    lazy var noSearchImageView: UIImageView = {
        let imageView = UIImageView.init(image: UIImage.init(named: "noSearchMembers"))
        imageView.isUserInteractionEnabled = false
        return imageView
    }()
    
    lazy var noMembersTip: UILabel = {
        let label = UILabel()
        label.text = .backgroundTipsText
        label.numberOfLines = 2
        label.textAlignment = NSTextAlignment.center
        label.textColor = UIColor(hex: "BBBBBB")
        return label
    }()
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        navigationController?.navigationBar.barTintColor = .white
        navigationController?.navigationBar.titleTextAttributes = [.foregroundColor : UIColor.black];
        title = TRTCCallingLocalize("Demo.TRTC.calling.calltitle")
        TRTCCalling.shareInstance().login(sdkAppID: UInt32(SDKAPPID), user: ProfileManager.shared.curUserID() ?? "", userSig: ProfileManager.shared.curUserSig()) {
            
        } failed: { (_, _) in
            
        }

        let backBtn = UIButton(type: .custom)
        backBtn.setImage(UIImage(named: "calling_back"), for: .normal)
        backBtn.addTarget(self, action: #selector(backBtnClick), for: .touchUpInside)
        let item = UIBarButtonItem(customView: backBtn)
        item.tintColor = .black
        navigationItem.leftBarButtonItem = item
        setupUI()
        registerButtonTouchEvents()
    }
    
    @objc func backBtnClick() {
        let alertVC = UIAlertController.init(title: TRTCCallingLocalize("App.PortalViewController.areyousureloginout"), message: nil, preferredStyle: .alert)
        let cancelAction = UIAlertAction.init(title: TRTCCallingLocalize("App.PortalViewController.cancel"), style: .cancel, handler: nil)
        let sureAction = UIAlertAction.init(title: TRTCCallingLocalize("App.PortalViewController.determine"), style: .default) { (action) in
            ProfileManager.shared.removeLoginCache()
            AppUtils.shared.appDelegate.showLoginViewController()
            V2TIMManager.sharedInstance()?.logout({
                
            }, fail: { (errCode, errMsg) in
                
            })
        }
        alertVC.addAction(cancelAction)
        alertVC.addAction(sureAction)
        present(alertVC, animated: true, completion: nil)
    }
    
    deinit {
        debugPrint("deinit \(self)")
    }
    
    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.setNavigationBarHidden(false, animated: false)
        searchBar.text = ""
        shouldShowSearchResult = false
        // 每次进入页面的时候，刷新手机号
        //TODO: 此处需要从IM获取到用户手机号
        userInfoLabel.text = "\(String.yourUserNameText)\("\(V2TIMManager.sharedInstance()?.getLoginUser() ?? "")")"
    }
    
    public override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        navigationController?.setNavigationBarHidden(false, animated: false)
    }
    
    public func onError(code: Int32, msg: String?) {
        debugPrint("onError: code \(code), msg: \(String(describing: msg))")
    }
    
    public func onInvited(sponsor: String, userIds: [String], isFromGroup: Bool, callType: CallType) {
        debugPrint("log: onInvited sponsor:\(sponsor) userIds:\(userIds)")
        self.callType  = callType
        //TODO: 查询用户信息，调用IM
        V2TIMManager.sharedInstance()?.getUsersInfo([sponsor], succ: { [weak self] (userInfos) in
            guard let `self` = self, let curUserInfo = userInfos?.first else { return }
            V2TIMManager.sharedInstance()?.getUsersInfo(userIds, succ: { [weak self] (userInfos) in
                guard let `self` = self, let userInfos = userInfos else { return }
                var list: [CallUserModel] = []
                for userInfo in userInfos {
                    list.append(self.covertUser(user: userInfo))
                }
                self.showCallVC(invitedList: list, sponsor: self.covertUser(user: curUserInfo, isEnter: true))
            }, fail: { (_, _) in
            })
        }, fail: { (_,_) in
        })
    }
    
    private func onGroupCallInviteeListUpdate(userIds: [String]) {
        debugPrint("log: onGroupCallInviteeListUpdate userIds:\(userIds)")
    }
    
    public func onUserEnter(uid: String) {
        debugPrint("log: onUserEnter: \(uid)")
        if let vc = callVC {
            //TODO: 查询用户信息，调用IM
            V2TIMManager.sharedInstance()?.getUsersInfo([uid], succ: { [weak self, weak vc] (userInfos) in
                guard let `self` = self, let vc = vc, let userInfo = userInfos?.first else { return }
                vc.enterUser(user: self.covertUser(user: userInfo))
                vc.view.makeToast("\(String(describing: userInfo.nickName)) \(String.enterConvText)")
            }, fail: { (_, _) in
            })
        }
    }
    
    public func onUserLeave(uid: String) {
        debugPrint("log: onUserLeave: \(uid)")
        removeUserFromCallVC(uid: uid, reason: .leave)
    }
    
    public func onReject(uid: String) {
        debugPrint("log: onReject: \(uid)")
        removeUserFromCallVC(uid: uid, reason: .reject)
    }
    
    public func onNoResp(uid: String) {
        debugPrint("log: onNoResp: \(uid)")
        removeUserFromCallVC(uid: uid, reason: .noresp)
    }
    
    public func onLineBusy(uid: String) {
        debugPrint("log: onLineBusy: \(uid)")
        removeUserFromCallVC(uid: uid, reason: .busy)
    }
    
    public func onCallingCancel(uid: String) {
        debugPrint("log: onCallingCancel")
        if let vc = callVC {
            view.makeToast("\((vc.curSponsor?.name) ?? "")\(String.cancelConvText)")
            vc.disMiss()
        }
    }
    
    public func onCallingTimeOut() {
        debugPrint("log: onCallingTimeOut")
        if let vc = callVC {
            view.makeToast(.callTimeOutText)
            vc.disMiss()
        }
    }
    
    public func onCallEnd() {
        debugPrint("log: onCallEnd")
        if let vc = callVC {
            vc.disMiss()
        }
    }
    
    public func onUserAudioAvailable(uid: String, available: Bool) {
        debugPrint("log: onUserAudioAvailable , uid: \(uid), available: \(available)")
    }
    
    public func onUserVoiceVolume(uid: String, volume: UInt32) {
        if let vc = callVC {
            if let user = vc.getUserById(userId: uid) {
                let newUser = user
                newUser.volume = Float(volume) / 100
                if callType == .audio {
                    vc.updateUser(user: newUser, animated: false)
                } else {
                    vc.updateUserVolume(user: newUser)
                }
                
            } else {
                guard let curUserID = ProfileManager.shared.curUserID() else {
                    return
                }
                if uid == curUserID { return }
                V2TIMManager.sharedInstance()?.getUsersInfo([uid], succ: { [weak vc] (userInfos) in
                    guard let vc = vc, let userInfo = userInfos?.first else { return }
                    vc.enterUser(user: self.covertUser(user: userInfo, volume: volume, isEnter: true))
                }, fail: { (_, _) in
                })
            }
        }
    }
    
    public func onUserVideoAvailable(uid: String, available: Bool) {
        debugPrint("log: onUserVideoAvailable , uid: \(uid), available: \(available)")
        if let vc = callVC {
            if let user = vc.getUserById(userId: uid) {
                user.isEnter = true
                user.isVideoAvaliable = available
                vc.updateUser(user: user, animated: false)
            } else {
                //TODO: 获取用户信息，调用IM
                V2TIMManager.sharedInstance()?.getUsersInfo([uid], succ: { [weak self, weak vc] (userInfos) in
                    guard let `self` = self, let vc = vc, let userInfo = userInfos?.first else { return }
                    let newUser = self.covertUser(user: userInfo, isEnter: true)
                    newUser.isVideoAvaliable = available
                    vc.enterUser(user: newUser)
                }, fail: { (_, _) in
                })
            }
        }
    }
    
    func covertUser(user: V2TIMUserFullInfo,
                    volume: UInt32 = 0,
                    isEnter: Bool = false) -> CallUserModel {
        let dstUser = CallUserModel()
        dstUser.name = user.nickName
        dstUser.avatar = user.faceURL
        dstUser.userId = user.userID
        dstUser.isEnter = isEnter
        dstUser.volume = Float(volume) / 100
        if let vc = callVC {
            if let oldUser = vc.getUserById(userId: user.userID) {
                dstUser.isVideoAvaliable = oldUser.isVideoAvaliable
            }
        }
        return dstUser
    }
    
    func removeUserFromCallVC(uid: String, reason: CallingUserRemoveReason = .noresp) {
        if let vc = callVC {
            //TODO: 获取用户信息，调用IM
            V2TIMManager.sharedInstance()?.getUsersInfo([uid], succ: { [weak self, weak vc] (userInfos) in
                guard let `self` = self, let vc = vc, let userInfo = userInfos?.first else { return }
                let callUserModel = self.covertUser(user: userInfo)
                vc.leaveUser(user: callUserModel)
                var toast = callUserModel.name ?? ""
                switch reason {
                case .reject:
                    toast += .rejectToastText
                    break
                case .leave:
                    toast += .leaveToastText
                    break
                case .noresp:
                    toast += .norespToastText
                    break
                case .busy:
                    toast += .busyToastText
                    break
                }
                vc.view.makeToast(toast)
                self.view.makeToast(toast)
            }, fail: { (_, _) in
            })
        }
    }
}

extension TRTCCallingContactViewController {
    
    func setupUI() {
        constructViewHierarchy()
        activateConstraints()
        bindInteraction()
        setupUIStyle()
        NotificationCenter.default.addObserver(self, selector: #selector(hiddenNoMembersImg), name: NSNotification.Name("HiddenNoSearchVideoNotificationKey"), object: nil)
        selectTable.reloadData()
    }
    
    func constructViewHierarchy() {
        // 添加SearchBar
        view.addSubview(searchContainerView)
        searchContainerView.addSubview(searchBar)
        searchContainerView.addSubview(searchBtn)
        view.addSubview(userInfoLabel)
        view.addSubview(selectTable)
        selectTable.isHidden = true
        view.addSubview(noSearchImageView)
        view.addSubview(noMembersTip)
    }
    
    func activateConstraints() {
        searchContainerView.snp.makeConstraints { (make) in
            make.top.equalTo(view.safeAreaLayoutGuide.snp.top).offset(20)
            make.leading.equalTo(20)
            make.trailing.equalTo(-20)
            make.height.equalTo(40)
        }
        searchBar.snp.makeConstraints { (make) in
            make.top.leading.bottom.equalToSuperview()
            make.trailing.equalTo(searchBtn.snp.leading).offset(-10)
        }
        searchBtn.snp.makeConstraints { (make) in
            make.top.trailing.bottom.equalToSuperview()
            make.width.equalTo(60)
        }
        userInfoLabel.snp.makeConstraints { (make) in
            make.top.equalTo(searchContainerView.snp.bottom).offset(20)
            make.leading.equalTo(20)
            make.trailing.equalTo(-20)
            make.height.equalTo(20)
        }
        selectTable.snp.makeConstraints { (make) in
            make.top.equalTo(userInfoLabel.snp.bottom).offset(10)
            make.leading.trailing.equalTo(view)
            make.bottomMargin.equalTo(view)
        }
        noSearchImageView.snp.makeConstraints { (make) in
            make.top.equalTo(view.bounds.size.height/3.0)
            make.leading.equalTo(view.bounds.size.width*154.0/375)
            make.trailing.equalTo(-view.bounds.size.width*154.0/375)
            make.height.equalTo(view.bounds.size.width*67.0/375)
        }
        noMembersTip.snp.makeConstraints { (make) in
            make.top.equalTo(noSearchImageView.snp.bottom)
            make.width.equalTo(view.bounds.size.width)
            make.height.equalTo(60)
        }
    }
    
    func setupUIStyle() {
        if let textfield = searchBar.value(forKey: "searchField") as? UITextField {
            textfield.layer.cornerRadius = 10.0
            textfield.layer.masksToBounds = true
            textfield.textColor = .black
            textfield.backgroundColor = .clear
            textfield.leftViewMode = .always
            if let leftView = textfield.leftView as? UIImageView {
                leftView.image =  UIImage.init(named: "cm_search_white")
            }
        }
        ToastManager.shared.position = .bottom
    }
    
    func bindInteraction() {
        searchBar.delegate = self
        // 设置选择通话用户结束后的交互逻辑
        selectedFinished = { [weak self] users in
            guard let `self` = self else {return}
            self.showChooseCallTypeSheet(users: users)
        }
    }
    
    func showChooseCallTypeSheet(users: [V2TIMUserFullInfo]) {
        let sheetVC = UIAlertController.init(title: TRTCCallingLocalize("Demo.TRTC.calling.callType") , message: "", preferredStyle: .actionSheet)
        let audioAction = UIAlertAction.init(title: TRTCCallingLocalize("Audio Call"), style: .default) { [weak self] _ in
            guard let `self` = self else { return }
            self.callType = .audio
            var list:[CallUserModel] = []
            var userIds: [String] = []
            for userModel in users {
                list.append(self.covertUser(user: userModel))
                userIds.append(userModel.userID)
            }
            self.showCallVC(invitedList: list)
            TRTCCalling.shareInstance().groupCall(userIDs:userIds, type: self.callType, groupID: nil)
        }
        
        let videoAction = UIAlertAction.init(title: TRTCCallingLocalize("Video Call"), style: .default) { [weak self] _ in
            guard let `self` = self else { return }
            self.callType = .video
            var list:[CallUserModel] = []
            var userIds: [String] = []
            for userModel in users {
                list.append(self.covertUser(user: userModel))
                userIds.append(userModel.userID)
            }
            self.showCallVC(invitedList: list)
            TRTCCalling.shareInstance().groupCall(userIDs:userIds, type: self.callType, groupID: nil)
        }
        sheetVC.addAction(audioAction)
        sheetVC.addAction(videoAction)
        present(sheetVC, animated: true, completion: nil)
    }
    
    @objc func hiddenNoMembersImg() {
        noSearchImageView.removeFromSuperview()
        noMembersTip.removeFromSuperview()
        selectTable.isHidden = false
    }
    
    /// show calling view
    /// - Parameters:
    ///   - invitedList: invitee userlist
    ///   - sponsor: passive call should not be nil,
    ///     otherwise sponsor call this mothed should ignore this parameter
    func showCallVC(invitedList: [CallUserModel], sponsor: CallUserModel? = nil) {
        if callType == .audio {
            callVC = TRTCCallingAuidoViewController.init(sponsor: sponsor)
        } else {
            callVC = TRTCCallingVideoViewController.init(sponsor: sponsor)
        }
        
        callVC?.dismissBlock = {[weak self] in
            guard let self = self else {return}
            self.callVC = nil
        }
        if let vc = callVC {
            vc.modalPresentationStyle = .fullScreen
            vc.resetWithUserList(users: invitedList, isInit: true)
            
            if var topController = UIApplication.shared.keyWindow?.rootViewController {
                while let presentedViewController = topController.presentedViewController {
                    topController = presentedViewController
                }
                if let navigationVC = topController as? UINavigationController {
                    if navigationVC.viewControllers.contains(self) {
                        present(vc, animated: false, completion: nil)
                    } else {
                        navigationVC.popToRootViewController(animated: false)
                        navigationVC.pushViewController(self, animated: false)
                        navigationVC.present(vc, animated: false, completion: nil)
                    }
                } else {
                    topController.present(vc, animated: false, completion: nil)
                }
            }
        }
    }
    
}

extension TRTCCallingContactViewController: UITextFieldDelegate, UISearchBarDelegate {
    
    public func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        searchBar.resignFirstResponder()
        if let input = searchBar.text, input.count > 0 {
            searchUser(input: input)
        }
    }
    
    public func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        if searchBar.text == nil || searchBar.text?.count ?? 0 == 0 {
            //show recent table
            shouldShowSearchResult = false
        }
        
        if (searchBar.text?.count ?? 0) > 11 {
            searchBar.text = (searchBar.text as NSString?)?.substring(to: 11)
        }
    }
    
    public func searchUser(input: String)  {
        //TODO: 获取用户信息，调用IM
        V2TIMManager.sharedInstance()?.getUsersInfo([input], succ: { [weak self] (userInfos) in
            guard let `self` = self, let userInfo = userInfos?.first else { return }
            self.searchResult = userInfo
            self.shouldShowSearchResult = true
            self.selectTable.reloadData()
            NotificationCenter.default.post(name: NSNotification.Name("HiddenNoSearchVideoNotificationKey"), object: nil)
        }, fail: { [weak self] (_, _) in
            guard let self = self else {return}
            self.searchResult = nil
            self.view.makeToast(.failedSearchText)
        })
    }
}

extension TRTCCallingContactViewController: UITableViewDelegate, UITableViewDataSource {
    
    //MARK: - UITableViewDataSource
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if shouldShowSearchResult {
            return 1
        }
        return 0
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "CallingSelectUserTableViewCell") as! CallingSelectUserTableViewCell
        cell.selectionStyle = .none
        if shouldShowSearchResult {
            if let user = searchResult {
                cell.config(model: user, selected: false) { [weak self] in
                    guard let self = self else { return }
                    if user.userID == V2TIMManager.sharedInstance()?.getLoginUser() {
                        self.view.makeToast(.cantInvateSelfText)
                        return
                    }
                    if let finish = self.selectedFinished {
                        finish([user])
                    }
                }
            } else {
                debugPrint("not search result")
            }
        }
        return cell
    }
    
    public func tableView(_ tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        view.tintColor = .clear
        let header = view as! UITableViewHeaderFooterView
        header.textLabel?.textColor = UIColor.white
    }
    
    public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 60
    }
    
    public func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return true
    }
}

extension TRTCCallingContactViewController {
    private func registerButtonTouchEvents() {
        searchBtn.addTarget(self, action: #selector(searchBtnTouchEvent(sender:)), for: .touchUpInside)
    }
    
    @objc private func searchBtnTouchEvent(sender: UIButton) {
        self.searchBar.resignFirstResponder()
        if let input = self.searchBar.text, input.count > 0 {
            self.searchUser(input: input)
        }
    }
}

private extension String {
    static let yourUserNameText = TRTCCallingLocalize("Demo.TRTC.calling.youruserId")
    static let searchPhoneNumberText = TRTCCallingLocalize("Demo.TRTC.calling.searchUserId")
    static let searchText = TRTCCallingLocalize("Demo.TRTC.calling.searching")
    static let backgroundTipsText = TRTCCallingLocalize("Demo.TRTC.calling.searchandcall")
    static let enterConvText = TRTCCallingLocalize("Demo.TRTC.calling.callingbegan")
    static let cancelConvText = TRTCCallingLocalize("Demo.TRTC.calling.callingcancel")
    static let callTimeOutText = TRTCCallingLocalize("Demo.TRTC.calling.callingtimeout")
    static let rejectToastText = TRTCCallingLocalize("Demo.TRTC.calling.callingrefuse")
    static let leaveToastText = TRTCCallingLocalize("Demo.TRTC.calling.callingleave")
    static let norespToastText = TRTCCallingLocalize("Demo.TRTC.calling.callingnoresponse")
    static let busyToastText = TRTCCallingLocalize("Demo.TRTC.calling.callingbusy")
    static let failedSearchText = TRTCCallingLocalize("Demo.TRTC.calling.searchingfailed")
    static let cantInvateSelfText = TRTCCallingLocalize("Demo.TRTC.calling.cantinviteself")
}
