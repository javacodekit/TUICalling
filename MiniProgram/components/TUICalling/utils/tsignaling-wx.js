!function(t, e) {
  'object' === typeof exports && 'object' === typeof module ? module.exports = e(require('tim-wx-sdk')) : 'function' === typeof define && define.amd ? define(['tim-wx-sdk'], e) : 'object' === typeof exports ? exports.TSignaling = e(require('tim-wx-sdk')) : t.TSignaling = e(t.TIM)
}(window, (function(t) {
  return function(t) {
    const e = {}; function i(n) {
      if (e[n]) return e[n].exports; const o = e[n] = { i: n, l: !1, exports: {} }; return t[n].call(o.exports, o, o.exports, i), o.l = !0, o.exports
    } return i.m = t, i.c = e, i.d = function(t, e, n) {
      i.o(t, e) || Object.defineProperty(t, e, { enumerable: !0, get: n })
    }, i.r = function(t) {
      'undefined' !== typeof Symbol && Symbol.toStringTag && Object.defineProperty(t, Symbol.toStringTag, { value: 'Module' }), Object.defineProperty(t, '__esModule', { value: !0 })
    }, i.t = function(t, e) {
      if (1 & e && (t = i(t)), 8 & e) return t; if (4 & e && 'object' === typeof t && t && t.__esModule) return t; const n = Object.create(null); if (i.r(n), Object.defineProperty(n, 'default', { enumerable: !0, value: t }), 2 & e && 'string' !== typeof t) {
        for (const o in t) {
          i.d(n, o, function(e) {
            return t[e]
          }.bind(null, o))
        }
      } return n
    }, i.n = function(t) {
      const e = t && t.__esModule ? function() {
        return t.default
      } : function() {
        return t
      }; return i.d(e, 'a', e), e
    }, i.o = function(t, e) {
      return Object.prototype.hasOwnProperty.call(t, e)
    }, i.p = '', i(i.s = 5)
  }([function(t, e, i) {
    'use strict'; i.d(e, 'e', (function() {
      return p
    })), i.d(e, 'g', (function() {
      return h
    })), i.d(e, 'c', (function() {
      return g
    })), i.d(e, 'f', (function() {
      return v
    })), i.d(e, 'b', (function() {
      return m
    })), i.d(e, 'd', (function() {
      return T
    })), i.d(e, 'a', (function() {
      return y
    })), i.d(e, 'h', (function() {
      return N
    })); const n = 'undefined' !== typeof window; const o = ('undefined' !== typeof wx && wx.getSystemInfoSync, n && window.navigator && window.navigator.userAgent || ''); const r = /AppleWebKit\/([\d.]+)/i.exec(o); const s = (r && parseFloat(r.pop()), /iPad/i.test(o)); const a = /iPhone/i.test(o) && !s; const u = /iPod/i.test(o); const c = a || s || u; const l = (function() {
      const t = o.match(/OS (\d+)_/i); t && t[1] && t[1]
    }(), /Android/i.test(o)); const d = function() {
      const t = o.match(/Android (\d+)(?:\.(\d+))?(?:\.(\d+))*/i); if (!t) return null; const e = t[1] && parseFloat(t[1]); const i = t[2] && parseFloat(t[2]); return e && i ? parseFloat(t[1] + '.' + t[2]) : e || null
    }(); const f = (l && /webkit/i.test(o), /Firefox/i.test(o), /Edge/i.test(o)); const _ = !f && /Chrome/i.test(o); const I = (function() {
      const t = o.match(/Chrome\/(\d+)/); t && t[1] && parseFloat(t[1])
    }(), /MSIE/.test(o), /MSIE\s8\.0/.test(o), function() {
      const t = /MSIE\s(\d+)\.\d/.exec(o); let e = t && parseFloat(t[1]); !e && /Trident\/7.0/i.test(o) && /rv:11.0/.test(o) && (e = 11)
    }(), /Safari/i.test(o), /TBS\/\d+/i.test(o)); (function() {
      const t = o.match(/TBS\/(\d+)/i); if (t && t[1])t[1]
    })(), !I && /MQQBrowser\/\d+/i.test(o), !I && / QQBrowser\/\d+/i.test(o), /(micromessenger|webbrowser)/i.test(o), /Windows/i.test(o), /MAC OS X/i.test(o), /MicroMessenger/i.test(o); i(2), i(1); const p = function(t) {
      return 'map' === D(t)
    }; const h = function(t) {
      return 'set' === D(t)
    }; const g = function(t) {
      return 'file' === D(t)
    }; const v = function(t) {
      if ('object' !== typeof t || null === t) return !1; const e = Object.getPrototypeOf(t); if (null === e) return !0; let i = e; for (;null !== Object.getPrototypeOf(i);)i = Object.getPrototypeOf(i); return e === i
    }; const E = function(t) {
      return 'function' === typeof Array.isArray ? Array.isArray(t) : 'array' === D(t)
    }; const m = function(t) {
      return E(t) || function(t) {
        return null !== t && 'object' === typeof t
      }(t)
    }; const T = function(t) {
      return t instanceof Error
    }; const D = function(t) {
      return Object.prototype.toString.call(t).match(/^\[object (.*)\]$/)[1].toLowerCase()
    }; let S = 0; Date.now || (Date.now = function() {
      return (new Date).getTime()
    }); const y = { now: function() {
      0 === S && (S = Date.now() - 1); const t = Date.now() - S; return t > 4294967295 ? (S += 4294967295, Date.now() - S) : t
    }, utc: function() {
      return Math.round(Date.now() / 1e3)
    } }; const N = function(t) {
      return JSON.stringify(t, ['message', 'code'])
    }
  }, function(t, e, i) {
    'use strict'; i.r(e); const n = i(3); const o = i(0); let r = 0; const s = new Map; function a() {
      const t = new Date; return 'TSignaling ' + t.toLocaleTimeString('en-US', { hour12: !1 }) + '.' + function(t) {
        let e; switch (t.toString().length) {
          case 1: e = '00' + t; break; case 2: e = '0' + t; break; default: e = t
        } return e
      }(t.getMilliseconds()) + ':'
    } const u = { _data: [], _length: 0, _visible: !1, arguments2String(t) {
      let e; if (1 === t.length)e = a() + t[0]; else {
        e = a(); for (let i = 0, n = t.length; i < n; i++)Object(o.b)(t[i]) ? Object(o.d)(t[i]) ? e += Object(o.h)(t[i]) : e += JSON.stringify(t[i]) : e += t[i], e += ' '
      } return e
    }, debug: function() {
      if (r <= -1) {
        const t = this.arguments2String(arguments); u.record(t, 'debug'), n.a.debug(t)
      }
    }, log: function() {
      if (r <= 0) {
        const t = this.arguments2String(arguments); u.record(t, 'log'), n.a.log(t)
      }
    }, info: function() {
      if (r <= 1) {
        const t = this.arguments2String(arguments); u.record(t, 'info'), n.a.info(t)
      }
    }, warn: function() {
      if (r <= 2) {
        const t = this.arguments2String(arguments); u.record(t, 'warn'), n.a.warn(t)
      }
    }, error: function() {
      if (r <= 3) {
        const t = this.arguments2String(arguments); u.record(t, 'error'), n.a.error(t)
      }
    }, time: function(t) {
      s.set(t, o.a.now())
    }, timeEnd: function(t) {
      if (s.has(t)) {
        const e = o.a.now() - s.get(t); return s.delete(t), e
      } return n.a.warn(`未找到对应label: ${t}, 请在调用 logger.timeEnd 前，调用 logger.time`), 0
    }, setLevel: function(t) {
      t < 4 && n.a.log(a() + 'set level from ' + r + ' to ' + t), r = t
    }, record: function(t, e) {
      1100 === u._length && (u._data.splice(0, 100), u._length = 1e3), u._length++, u._data.push(`${t} [${e}] \n`)
    }, getLog: function() {
      return u._data
    } }; e.default = u
  }, function(t, e, i) {
    'use strict'; i.r(e); e.default = { MSG_PRIORITY_HIGH: 'High', MSG_PRIORITY_NORMAL: 'Normal', MSG_PRIORITY_LOW: 'Low', MSG_PRIORITY_LOWEST: 'Lowest', KICKED_OUT_MULT_ACCOUNT: 'multipleAccount', KICKED_OUT_MULT_DEVICE: 'multipleDevice', KICKED_OUT_USERSIG_EXPIRED: 'userSigExpired', NET_STATE_CONNECTED: 'connected', NET_STATE_CONNECTING: 'connecting', NET_STATE_DISCONNECTED: 'disconnected', ENTER_ROOM_SUCCESS: 'JoinedSuccess', ALREADY_IN_ROOM: 'AlreadyInGroup' }
  }, function(t, e, i) {
    'use strict'; (function(t) {
      let i; let n; i = 'undefined' !== typeof console ? console : void 0 !== t && t.console ? t.console : 'undefined' !== typeof window && window.console ? window.console : {}; const o = function() {}; const r = ['assert', 'clear', 'count', 'debug', 'dir', 'dirxml', 'error', 'exception', 'group', 'groupCollapsed', 'groupEnd', 'info', 'log', 'markTimeline', 'profile', 'profileEnd', 'table', 'time', 'timeEnd', 'timeStamp', 'trace', 'warn']; let s = r.length; for (;s--;)n = r[s], console[n] || (i[n] = o); i.methods = r, e.a = i
    }).call(this, i(8))
  }, function(t, e, i) {
    'use strict'; Object.defineProperty(e, '__esModule', { value: !0 }); e.default = { NEW_INVITATION_RECEIVED: 'ts_new_invitation_received', INVITEE_ACCEPTED: 'ts_invitee_accepted', INVITEE_REJECTED: 'ts_invitee_rejected', INVITATION_CANCELLED: 'ts_invitation_cancelled', INVITATION_TIMEOUT: 'ts_invitation_timeout', SDK_READY: 'ts_im_ready', SDK_NOT_READY: 'ts_im_not_ready', TEXT_MESSAGE_RECEIVED: 'ts_text_message_received', CUSTOM_MESSAGE_RECEIVED: 'ts_custom_message_received', REMOTE_USER_JOIN: 'ts_remote_user_join', REMOTE_USER_LEAVE: 'ts_remote_user_leave', KICKED_OUT: 'ts_kicked_out', NET_STATE_CHANGE: 'ts_net_state_change' }
  }, function(t, e, i) {
    'use strict'; const n = this && this.__awaiter || function(t, e, i, n) {
      return new (i || (i = Promise))((function(o, r) {
        function s(t) {
          try {
            u(n.next(t))
          } catch (t) {
            r(t)
          }
        } function a(t) {
          try {
            u(n.throw(t))
          } catch (t) {
            r(t)
          }
        } function u(t) {
          let e; t.done ? o(t.value) : (e = t.value, e instanceof i ? e : new i((function(t) {
            t(e)
          }))).then(s, a)
        }u((n = n.apply(t, e || [])).next())
      }))
    }; const o = this && this.__generator || function(t, e) {
      let i; let n; let o; let r; let s = { label: 0, sent: function() {
        if (1 & o[0]) throw o[1]; return o[1]
      }, trys: [], ops: [] }; return r = { next: a(0), throw: a(1), return: a(2) }, 'function' === typeof Symbol && (r[Symbol.iterator] = function() {
        return this
      }), r; function a(r) {
        return function(a) {
          return function(r) {
            if (i) throw new TypeError('Generator is already executing.'); for (;s;) {
              try {
                if (i = 1, n && (o = 2 & r[0] ? n.return : r[0] ? n.throw || ((o = n.return) && o.call(n), 0) : n.next) && !(o = o.call(n, r[1])).done) return o; switch (n = 0, o && (r = [2 & r[0], o.value]), r[0]) {
                  case 0: case 1: o = r; break; case 4: return s.label++, { value: r[1], done: !1 }; case 5: s.label++, n = r[1], r = [0]; continue; case 7: r = s.ops.pop(), s.trys.pop(); continue; default: if (!(o = s.trys, (o = o.length > 0 && o[o.length - 1]) || 6 !== r[0] && 2 !== r[0])) {
                    s = 0; continue
                  } if (3 === r[0] && (!o || r[1] > o[0] && r[1] < o[3])) {
                      s.label = r[1]; break
                    } if (6 === r[0] && s.label < o[1]) {
                      s.label = o[1], o = r; break
                    } if (o && s.label < o[2]) {
                      s.label = o[2], s.ops.push(r); break
                    }o[2] && s.ops.pop(), s.trys.pop(); continue
                }r = e.call(t, s)
              } catch (t) {
                r = [6, t], n = 0
              } finally {
                i = o = 0
              }
            } if (5 & r[0]) throw r[1]; return { value: r[0] ? r[1] : void 0, done: !0 }
          }([r, a])
        }
      }
    }; const r = this && this.__spreadArrays || function() {
      for (var t = 0, e = 0, i = arguments.length; e < i; e++)t += arguments[e].length; const n = Array(t); let o = 0; for (e = 0; e < i; e++) for (let r = arguments[e], s = 0, a = r.length; s < a; s++, o++)n[o] = r[s]; return n
    }; Object.defineProperty(e, '__esModule', { value: !0 }); const s = i(6); const a = i(7); const u = i(4); const c = i(2); const l = i(1); const d = i(9); const f = i(10); const _ = i(11); const I = i(12); const p = i(14); const h = i(15).version; const g = function() {
      function t(t) {
        if (this._outerEmitter = null, this._safetyCallbackFactory = null, this._tim = null, this._imSDKAppID = 0, this._userID = null, this._groupID = '', this._isHandling = !1, this._inviteInfoMap = new Map, l.default.info('TSignaling version:' + h), d.default(t.SDKAppID)) return l.default.error('TSignaling 请传入 SDKAppID !!!'), null; this._outerEmitter = new a.default, this._outerEmitter._emit = this._outerEmitter.emit, this._outerEmitter.emit = function() {
          for (var t = [], e = 0; e < arguments.length; e++)t[e] = arguments[e]; const i = t[0]; const n = [i, { name: t[0], data: t[1] }]; this._outerEmitter._emit.apply(this._outerEmitter, r(n))
        }.bind(this), this._safetyCallbackFactory = new _.default, t.tim ? this._tim = t.tim : this._tim = p.create({ SDKAppID: t.SDKAppID, scene: 'TSignaling' }), this._imSDKAppID = t.SDKAppID, this._tim.on(p.EVENT.SDK_READY, this._onIMReady.bind(this)), this._tim.on(p.EVENT.SDK_NOT_READY, this._onIMNotReady.bind(this)), this._tim.on(p.EVENT.KICKED_OUT, this._onKickedOut.bind(this)), this._tim.on(p.EVENT.NET_STATE_CHANGE, this._onNetStateChange.bind(this)), this._tim.on(p.EVENT.MESSAGE_RECEIVED, this._onMessageReceived.bind(this))
      } return t.prototype.setLogLevel = function(t) {
        l.default.setLevel(t), this._tim.setLogLevel(t)
      }, t.prototype.login = function(t) {
        return n(this, void 0, void 0, (function() {
          return o(this, (function(e) {
            return l.default.log('TSignaling.login', t), this._userID = t.userID, [2, this._tim.login(t)]
          }))
        }))
      }, t.prototype.logout = function() {
        return n(this, void 0, void 0, (function() {
          return o(this, (function(t) {
            return l.default.log('TSignaling.logout'), this._userID = '', this._inviteInfoMap.clear(), [2, this._tim.logout()]
          }))
        }))
      }, t.prototype.on = function(t, e, i) {
        l.default.log('TSignaling.on eventName:' + t), this._outerEmitter.on(t, this._safetyCallbackFactory.defense(t, e, i), i)
      }, t.prototype.off = function(t, e) {
        l.default.log('TSignaling.off eventName:' + t), this._outerEmitter.off(t, e)
      }, t.prototype.joinGroup = function(t) {
        return n(this, void 0, void 0, (function() {
          return o(this, (function(e) {
            return l.default.log('TSignaling.joinGroup groupID:' + t), this._groupID = t, [2, this._tim.joinGroup({ groupID: t })]
          }))
        }))
      }, t.prototype.quitGroup = function(t) {
        return n(this, void 0, void 0, (function() {
          return o(this, (function(e) {
            return l.default.log('TSignaling.quitGroup groupID:' + t), [2, this._tim.quitGroup(t)]
          }))
        }))
      }, t.prototype.sendTextMessage = function(t) {
        return n(this, void 0, void 0, (function() {
          let e; return o(this, (function(i) {
            return e = this._tim.createTextMessage({ to: t.to, conversationType: !0 === t.groupFlag ? p.TYPES.CONV_GROUP : p.TYPES.CONV_C2C, priority: t.priority || p.TYPES.MSG_PRIORITY_NORMAL, payload: { text: t.text } }), [2, this._tim.sendMessage(e)]
          }))
        }))
      }, t.prototype.sendCustomMessage = function(t) {
        return n(this, void 0, void 0, (function() {
          let e; return o(this, (function(i) {
            return e = this._tim.createCustomMessage({ to: t.to, conversationType: !0 === t.groupFlag ? p.TYPES.CONV_GROUP : p.TYPES.CONV_C2C, priority: t.priority || p.TYPES.MSG_PRIORITY_NORMAL, payload: { data: t.data || '', description: t.description || '', extension: t.extension || '' } }), [2, this._tim.sendMessage(e)]
          }))
        }))
      }, t.prototype.invite = function(t) {
        return n(this, void 0, void 0, (function() {
          let e; let i; let n; let r; let a; let u; return o(this, (function(o) {
            switch (o.label) {
              case 0: return e = I.generate(), l.default.log('TSignaling.invite', t, 'inviteID=' + e), d.default(t) || d.default(t.userID) ? [2, Promise.reject(new f.default({ code: s.ErrorCode.ERR_INVALID_PARAMETERS, message: 'userID is invalid' }))] : (i = t.userID, n = t.data, r = t.timeout, a = { businessID: s.BusinessID.SIGNAL, inviteID: e, inviter: this._userID, actionType: s.ActionType.INVITE, inviteeList: [i], data: n, timeout: d.default(r) ? 0 : r, groupID: '' }, [4, this._sendCustomMessage(i, a)]); case 1: return 0 === (u = o.sent()).code ? (l.default.log('TSignaling.invite ok'), this._inviteInfoMap.set(e, a), this._startTimer(a, !0), [2, { inviteID: e, code: u.code, data: u.data }]) : [2, u]
            }
          }))
        }))
      }, t.prototype.inviteInGroup = function(t) {
        return n(this, void 0, void 0, (function() {
          let e; let i; let n; let r; let a; let u; let c; return o(this, (function(o) {
            switch (o.label) {
              case 0: return e = I.generate(), l.default.log('TSignaling.inviteInGroup', t, 'inviteID=' + e), d.default(t) || d.default(t.groupID) ? [2, Promise.reject(new f.default({ code: s.ErrorCode.ERR_INVALID_PARAMETERS, message: 'groupID is invalid' }))] : (i = t.groupID, n = t.inviteeList, r = t.data, a = t.timeout, u = { businessID: s.BusinessID.SIGNAL, inviteID: e, inviter: this._userID, actionType: s.ActionType.INVITE, inviteeList: n, data: r, timeout: d.default(a) ? 0 : a, groupID: i }, [4, this._sendCustomMessage(i, u)]); case 1: return 0 === (c = o.sent()).code ? (l.default.log('TSignaling.inviteInGroup ok'), this._inviteInfoMap.set(e, u), this._startTimer(u, !0), [2, { inviteID: e, code: c.code, data: c.data }]) : [2, c]
            }
          }))
        }))
      }, t.prototype.cancel = function(t) {
        return n(this, void 0, void 0, (function() {
          let e; let i; let n; let r; let a; let u; let c; let _; let I; return o(this, (function(o) {
            switch (o.label) {
              case 0: return l.default.log('TSignaling.cancel', t), d.default(t) || d.default(t.inviteID) || !this._inviteInfoMap.has(t.inviteID) || this._isHandling ? [2, Promise.reject(new f.default({ code: s.ErrorCode.ERR_SDK_SIGNALING_INVALID_INVITE_ID, message: 'inviteID is invalid or invitation has been processed' }))] : (this._isHandling = !0, e = t.inviteID, i = t.data, n = this._inviteInfoMap.get(e), r = n.inviter, a = n.groupID, u = n.inviteeList, r !== this._userID ? [3, 2] : (c = { businessID: s.BusinessID.SIGNAL, inviteID: e, inviter: r, actionType: s.ActionType.CANCEL_INVITE, inviteeList: u, data: i, timeout: 0, groupID: a }, _ = a || u[0], [4, this._sendCustomMessage(_, c)])); case 1: return I = o.sent(), this._isHandling = !1, I && 0 === I.code ? (l.default.log('TSignaling.cancel ok'), this._deleteInviteInfoByID(e), [2, { inviteID: e, code: I.code, data: I.data }]) : [2, I]; case 2: return l.default.error('TSignaling.cancel unmatched inviter=' + r + ' and userID=' + this._userID), this._isHandling = !1, [2, Promise.reject(new f.default({ code: s.ErrorCode.ERR_SDK_SIGNALING_NO_PERMISSION, message: '信令请求无权限，比如取消非自己发起的邀请，接受或则拒绝非发给自己的邀请' }))]
            }
          }))
        }))
      }, t.prototype.accept = function(t) {
        return n(this, void 0, void 0, (function() {
          let e; let i; let n; let r; let a; let u; let c; let _; return o(this, (function(o) {
            switch (o.label) {
              case 0: return l.default.log('TSignaling.accept', t), d.default(t) || d.default(t.inviteID) || !this._inviteInfoMap.has(t.inviteID) || this._isHandling ? [2, Promise.reject(new f.default({ code: s.ErrorCode.ERR_SDK_SIGNALING_INVALID_INVITE_ID, message: 'inviteID is invalid or invitation has been processed' }))] : (this._isHandling = !0, e = t.inviteID, i = t.data, n = this._inviteInfoMap.get(e), r = n.inviter, a = n.groupID, n.inviteeList.includes(this._userID) ? (u = { businessID: s.BusinessID.SIGNAL, inviteID: e, inviter: r, actionType: s.ActionType.ACCEPT_INVITE, inviteeList: [this._userID], data: i, timeout: 0, groupID: a }, c = a || r, [4, this._sendCustomMessage(c, u)]) : [3, 2]); case 1: return _ = o.sent(), this._isHandling = !1, _ && 0 === _.code ? (l.default.log('TSignaling.accept ok'), this._updateLocalInviteInfo(u), [2, { inviteID: e, code: _.code, data: _.data }]) : [2, _]; case 2: return l.default.error('TSignaling.accept inviteeList do not include userID=' + this._userID + '. inviteID=' + e + ' groupID=' + a), this._isHandling = !1, [2, Promise.reject(new f.default({ code: s.ErrorCode.ERR_SDK_SIGNALING_INVALID_INVITE_ID, message: 'inviteID is invalid or invitation has been processed' }))]
            }
          }))
        }))
      }, t.prototype.reject = function(t) {
        return n(this, void 0, void 0, (function() {
          let e; let i; let n; let r; let a; let u; let c; let _; return o(this, (function(o) {
            switch (o.label) {
              case 0: return l.default.log('TSignaling.reject', t), d.default(t) || d.default(t.inviteID) || !this._inviteInfoMap.has(t.inviteID) || this._isHandling ? [2, Promise.reject(new f.default({ code: s.ErrorCode.ERR_SDK_SIGNALING_INVALID_INVITE_ID, message: 'inviteID is invalid or invitation has been processed' }))] : (this._isHandling = !0, e = t.inviteID, i = t.data, n = this._inviteInfoMap.get(e), r = n.inviter, a = n.groupID, n.inviteeList.includes(this._userID) ? (u = { businessID: s.BusinessID.SIGNAL, inviteID: e, inviter: r, actionType: s.ActionType.REJECT_INVITE, inviteeList: [this._userID], data: i, timeout: 0, groupID: a }, c = a || r, [4, this._sendCustomMessage(c, u)]) : [3, 2]); case 1: return _ = o.sent(), this._isHandling = !1, _ && 0 === _.code ? (l.default.log('TSignaling.reject ok'), this._updateLocalInviteInfo(u), [2, { inviteID: e, code: _.code, data: _.data }]) : [2, _]; case 2: return l.default.error('TSignaling.reject inviteeList do not include userID=' + this._userID + '. inviteID=' + e + ' groupID=' + a), this._isHandling = !1, [2, Promise.reject(new f.default({ code: s.ErrorCode.ERR_SDK_SIGNALING_INVALID_INVITE_ID, message: 'inviteID is invalid or invitation has been processed' }))]
            }
          }))
        }))
      }, t.prototype._onIMReady = function(t) {
        l.default.log('TSignaling._onIMReady'), this._outerEmitter.emit(u.default.SDK_READY)
      }, t.prototype._onIMNotReady = function(t) {
        l.default.log('TSignaling.onSdkNotReady'), this._outerEmitter.emit(u.default.SDK_NOT_READY)
      }, t.prototype._onKickedOut = function(t) {
        l.default.log('TSignaling._onKickedOut'), this._outerEmitter.emit(u.default.KICKED_OUT, t.data)
      }, t.prototype._onNetStateChange = function(t) {
        l.default.log('TSignaling._onNetStateChange'), this._outerEmitter.emit(u.default.NET_STATE_CHANGE, t.data)
      }, t.prototype._onMessageReceived = function(t) {
        const e = this; const i = t.data; const n = i.filter((function(t) {
          return t.type === p.TYPES.MSG_TEXT
        })); d.default(n) || this._outerEmitter.emit(u.default.TEXT_MESSAGE_RECEIVED, n); const o = i.filter((function(t) {
          return t.type === p.TYPES.MSG_GRP_TIP && t.payload.operationType === p.TYPES.GRP_TIP_MBR_JOIN
        })); d.default(o) || this._outerEmitter.emit(u.default.REMOTE_USER_JOIN, o); const r = i.filter((function(t) {
          return t.type === p.TYPES.MSG_GRP_TIP && t.payload.operationType === p.TYPES.GRP_TIP_MBR_QUIT
        })); d.default(r) || this._outerEmitter.emit(u.default.REMOTE_USER_LEAVE, r); const a = i.filter((function(t) {
          return t.type === p.TYPES.MSG_CUSTOM
        })); const c = []; a.forEach((function(t) {
          let i; const n = t.payload.data; let o = !0; try {
            i = JSON.parse(n)
          } catch (t) {
            o = !1
          } if (o) {
            const r = i.businessID; const a = i.actionType; if (1 === r) {
              switch (a) {
                case s.ActionType.INVITE: e._onNewInvitationReceived(i); break; case s.ActionType.REJECT_INVITE: e._onInviteeRejected(i); break; case s.ActionType.ACCEPT_INVITE: e._onInviteeAccepted(i); break; case s.ActionType.CANCEL_INVITE: e._onInvitationCancelled(i); break; case s.ActionType.INVITE_TIMEOUT: e._onInvitationTimeout(i)
              }
            } else {
              if ('av_call' === r) return !0; l.default.warn('TSignaling._onMessageReceived unknown businessID=' + r), c.push(t)
            }
          } else c.push(t)
        })), d.default(c) || this._outerEmitter.emit(u.default.CUSTOM_MESSAGE_RECEIVED, c)
      }, t.prototype._hasLocalInviteInfo = function(t, e) {
        const i = t.inviteID; const n = t.groupID; if (!this._inviteInfoMap.has(i)) return !1; const o = this._inviteInfoMap.get(i).inviteeList; return !n || (e ? o.length > 0 : o.length > 0 && o.includes(this._userID))
      }, t.prototype._startTimer = function(t, e) {
        const i = this; void 0 === e && (e = !0); const n = t.timeout; if (l.default.log('TSignaling._startTimer timeout=' + n + ' isInvitator=' + e), 0 !== n) var o = e ? n + 5 : n; let r = 1; var s = setInterval((function() {
          const n = i._hasLocalInviteInfo(t, e); r < o && n ? ++r : (n && i._sendTimeoutNotice(t, e), clearInterval(s))
        }), 1e3)
      }, t.prototype._sendTimeoutNotice = function(t, e) {
        return n(this, void 0, void 0, (function() {
          let i; let n; let r; let a; let c; let d; let f; let _; return o(this, (function(o) {
            switch (o.label) {
              case 0: return i = t.inviteID, n = t.groupID, r = t.inviteeList, a = t.inviter, c = t.data, d = e ? n || r[0] : n || a, l.default.log('TSignaling._sendTimeoutNotice inviteID=' + i + ' to=' + d + ' isInvitator=' + e), f = { businessID: s.BusinessID.SIGNAL, inviteID: i, inviter: a, actionType: s.ActionType.INVITE_TIMEOUT, inviteeList: e ? r : [this._userID], data: c, timeout: 0, groupID: n }, [4, this._sendCustomMessage(d, f)]; case 1: return (_ = o.sent()) && 0 === _.code && (this._outerEmitter.emit(u.default.INVITATION_TIMEOUT, { inviter: a, inviteID: i, groupID: n, inviteeList: f.inviteeList, isSelfTimeout: !0 }), e ? this._deleteInviteInfoByID(i) : this._updateLocalInviteInfo(f)), [2, _]
            }
          }))
        }))
      }, t.prototype._onNewInvitationReceived = function(e) {
        const i = e.inviteID; const n = e.inviter; const o = e.inviteeList; const r = e.groupID; const s = e.data; const a = o.includes(this._userID); l.default.log('TSignaling._onNewInvitationReceived', e, 'myselfIncluded=' + a); const u = JSON.parse(s); r && (0 === u.call_end || d.default(u.room_id) || d.default(u.data.room_id)) ? this._outerEmitter.emit(t.EVENT.NEW_INVITATION_RECEIVED, { inviteID: i, inviter: n, groupID: r, inviteeList: o, data: e.data || '' }) : (r && a || !r) && (this._inviteInfoMap.set(i, e), this._outerEmitter.emit(t.EVENT.NEW_INVITATION_RECEIVED, { inviteID: i, inviter: n, groupID: r, inviteeList: o, data: e.data || '' }), this._startTimer(e, !1))
      }, t.prototype._onInviteeRejected = function(t) {
        const e = t.inviteID; const i = t.inviter; const n = t.groupID; const o = this._inviteInfoMap.has(e); l.default.log('TSignaling._onInviteeRejected inviteID=' + e + ' hasInviteID=' + o + ' inviter=' + i + ' groupID=' + n), (n && o || !n) && (this._updateLocalInviteInfo(t), this._outerEmitter.emit(u.default.INVITEE_REJECTED, { inviteID: e, inviter: i, groupID: n, invitee: t.inviteeList[0], data: t.data || '' }))
      }, t.prototype._onInviteeAccepted = function(t) {
        const e = t.inviteID; const i = t.inviter; const n = t.groupID; const o = this._inviteInfoMap.has(e); l.default.log('TSignaling._onInviteeAccepted inviteID=' + e + ' hasInviteID=' + o + ' inviter=' + i + ' groupID=' + n), (n && o || !n) && (this._updateLocalInviteInfo(t), this._outerEmitter.emit(u.default.INVITEE_ACCEPTED, { inviteID: e, inviter: i, groupID: n, invitee: t.inviteeList[0], data: t.data || '' }))
      }, t.prototype._onInvitationCancelled = function(t) {
        const e = t.inviteID; const i = t.inviter; const n = t.groupID; const o = this._inviteInfoMap.has(e); l.default.log('TSignaling._onInvitationCancelled inviteID=' + e + ' hasInviteID=' + o + ' inviter=' + i + ' groupID=' + n), (n && o || !n) && (this._deleteInviteInfoByID(e), this._outerEmitter.emit(u.default.INVITATION_CANCELLED, { inviteID: e, inviter: i, groupID: n, data: t.data || '' }))
      }, t.prototype._onInvitationTimeout = function(t) {
        const e = t.inviteID; const i = t.inviter; const n = t.groupID; const o = this._inviteInfoMap.has(e); l.default.log('TSignaling._onInvitationTimeout inviteID=' + e + ' hasInviteID=' + o + ' inviter=' + i + ' groupID=' + n), (n && o || !n) && (this._updateLocalInviteInfo(t), this._outerEmitter.emit(u.default.INVITATION_TIMEOUT, { inviteID: e, inviter: i, groupID: n, inviteeList: t.inviteeList, isSelfTimeout: !1 }))
      }, t.prototype._updateLocalInviteInfo = function(t) {
        const e = t.inviteID; const i = t.inviter; const n = t.inviteeList; const o = t.groupID; if (l.default.log('TSignaling._updateLocalInviteInfo inviteID=' + e + ' inviter=' + i + ' groupID=' + o), o) {
          if (this._inviteInfoMap.has(e)) {
            const r = n[0]; const s = this._inviteInfoMap.get(e).inviteeList; s.includes(r) && (s.splice(s.indexOf(r), 1), l.default.log('TSignaling._updateLocalInviteInfo remove ' + r + ' from localInviteeList. ' + s.length + ' invitees left')), 0 === s.length && this._deleteInviteInfoByID(e)
          }
        } else this._deleteInviteInfoByID(e)
      }, t.prototype._deleteInviteInfoByID = function(t) {
        this._inviteInfoMap.has(t) && (l.default.log('TSignaling._deleteInviteInfoByID remove ' + t + ' from inviteInfoMap.'), this._inviteInfoMap.delete(t))
      }, t.prototype._sendCustomMessage = function(t, e) {
        return n(this, void 0, void 0, (function() {
          let i; let n; return o(this, (function(o) {
            return i = e.groupID, n = this._tim.createCustomMessage({ to: t, conversationType: i ? p.TYPES.CONV_GROUP : p.TYPES.CONV_C2C, priority: p.TYPES.MSG_PRIORITY_HIGH, payload: { data: JSON.stringify(e) } }), e.actionType === s.ActionType.INVITE ? [2, this._tim.sendMessage(n, { offlinePushInfo: { title: '', description: '您有一个通话请求', androidOPPOChannelID: '', extension: this._handleOfflineInfo(e) } })] : [2, this._tim.sendMessage(n)]
          }))
        }))
      }, t.prototype._handleOfflineInfo = function(t) {
        const e = { action: t.actionType, call_type: t.groupID ? 2 : 1, room_id: t.data.room_id, call_id: t.inviteID, timeout: t.data.timeout, version: 4, invited_list: t.inviteeList }; t.groupID && (e.group_id = t.groupID); const i = { entity: { action: 2, chatType: t.groupID ? 2 : 1, content: JSON.stringify(e), sendTime: parseInt(Date.now() / 1e3 + ''), sender: t.inviter, version: 1 } }; const n = JSON.stringify(i); return l.default.log('TSignaling._handleOfflineInfo ' + n), n
      }, t.EVENT = u.default, t.TYPES = c.default, t
    }(); e.default = g
  }, function(t, e, i) {
    'use strict'; let n; let o; let r; Object.defineProperty(e, '__esModule', { value: !0 }), e.ErrorCode = e.BusinessID = e.ActionType = void 0, function(t) {
      t[t.INVITE = 1] = 'INVITE', t[t.CANCEL_INVITE = 2] = 'CANCEL_INVITE', t[t.ACCEPT_INVITE = 3] = 'ACCEPT_INVITE', t[t.REJECT_INVITE = 4] = 'REJECT_INVITE', t[t.INVITE_TIMEOUT = 5] = 'INVITE_TIMEOUT'
    }(n || (n = {})), e.ActionType = n, function(t) {
      t[t.SIGNAL = 1] = 'SIGNAL'
    }(o || (o = {})), e.BusinessID = o, function(t) {
      t[t.ERR_INVALID_PARAMETERS = 6017] = 'ERR_INVALID_PARAMETERS', t[t.ERR_SDK_SIGNALING_INVALID_INVITE_ID = 8010] = 'ERR_SDK_SIGNALING_INVALID_INVITE_ID', t[t.ERR_SDK_SIGNALING_NO_PERMISSION = 8011] = 'ERR_SDK_SIGNALING_NO_PERMISSION'
    }(r || (r = {})), e.ErrorCode = r
  }, function(t, e, i) {
    'use strict'; i.r(e), i.d(e, 'default', (function() {
      return s
    })); const n = Function.prototype.apply; const o = new WeakMap; function r(t) {
      return o.has(t) || o.set(t, {}), o.get(t)
    } class s {
      constructor(t = null, e = console) {
        const i = r(this); return i._events = new Set, i._callbacks = {}, i._console = e, i._maxListeners = null === t ? null : parseInt(t, 10), this
      }_addCallback(t, e, i, n) {
        return this._getCallbacks(t).push({ callback: e, context: i, weight: n }), this._getCallbacks(t).sort((t, e)=>t.weight > e.weight), this
      }_getCallbacks(t) {
        return r(this)._callbacks[t]
      }_getCallbackIndex(t, e) {
        return this._has(t) ? this._getCallbacks(t).findIndex((t)=>t.callback === e) : null
      }_achieveMaxListener(t) {
        return null !== r(this)._maxListeners && r(this)._maxListeners <= this.listenersNumber(t)
      }_callbackIsExists(t, e, i) {
        const n = this._getCallbackIndex(t, e); const o = -1 !== n ? this._getCallbacks(t)[n] : void 0; return -1 !== n && o && o.context === i
      }_has(t) {
        return r(this)._events.has(t)
      }on(t, e, i = null, n = 1) {
        const o = r(this); if ('function' !== typeof e) throw new TypeError(e + ' is not a function'); return this._has(t) ? (this._achieveMaxListener(t) && o._console.warn(`Max listeners (${o._maxListeners}) for event "${t}" is reached!`), this._callbackIsExists(...arguments) && o._console.warn(`Event "${t}" already has the callback ${e}.`)) : (o._events.add(t), o._callbacks[t] = []), this._addCallback(...arguments), this
      }once(t, e, i = null, o = 1) {
        const r = (...o)=>(this.off(t, r), n.call(e, i, o)); return this.on(t, r, i, o)
      }off(t, e = null) {
        const i = r(this); let n; return this._has(t) && (null === e ? (i._events.delete(t), i._callbacks[t] = null) : (n = this._getCallbackIndex(t, e), -1 !== n && (i._callbacks[t].splice(n, 1), this.off(...arguments)))), this
      }emit(t, ...e) {
        return this._has(t) && this._getCallbacks(t).forEach((t)=>n.call(t.callback, t.context, e)), this
      }clear() {
        const t = r(this); return t._events.clear(), t._callbacks = {}, this
      }listenersNumber(t) {
        return this._has(t) ? this._getCallbacks(t).length : null
      }
    }
  }, function(t, e) {
    let i; i = function() {
      return this
    }(); try {
      i = i || new Function('return this')()
    } catch (t) {
      'object' === typeof window && (i = window)
    }t.exports = i
  }, function(t, e, i) {
    'use strict'; i.r(e); const n = i(0); const o = Object.prototype.hasOwnProperty; e.default = function(t) {
      if (null == t) return !0; if ('boolean' === typeof t) return !1; if ('number' === typeof t) return 0 === t; if ('string' === typeof t) return 0 === t.length; if ('function' === typeof t) return 0 === t.length; if (Array.isArray(t)) return 0 === t.length; if (t instanceof Error) return '' === t.message; if (Object(n.f)(t)) {
        for (const e in t) if (o.call(t, e)) return !1; return !0
      } return !!(Object(n.e)(t) || Object(n.g)(t) || Object(n.c)(t)) && 0 === t.size
    }
  }, function(t, e, i) {
    'use strict'; i.r(e); class n extends Error {
      constructor(t) {
        super(), this.code = t.code, this.message = t.message, this.data = t.data || {}
      }
    }e.default = n
  }, function(t, e, i) {
    'use strict'; i.r(e); const n = i(1); const o = i(4); const r = i.n(o); e.default = class {
      constructor() {
        this._funcMap = new Map
      }defense(t, e, i) {
        if ('string' !== typeof t) return null; if (0 === t.length) return null; if ('function' !== typeof e) return null; if (this._funcMap.has(t) && this._funcMap.get(t).has(e)) return this._funcMap.get(t).get(e); this._funcMap.has(t) || this._funcMap.set(t, new Map); let n = null; return this._funcMap.get(t).has(e) ? n = this._funcMap.get(t).get(e) : (n = this._pack(t, e, i), this._funcMap.get(t).set(e, n)), n
      }defenseOnce(t, e, i) {
        return 'function' !== typeof e ? null : this._pack(t, e, i)
      }find(t, e) {
        return 'string' !== typeof t || 0 === t.length || 'function' !== typeof e ? null : this._funcMap.has(t) ? this._funcMap.get(t).has(e) ? this._funcMap.get(t).get(e) : (n.default.log(`SafetyCallback.find: 找不到 func —— ${t}/${'' !== e.name ? e.name : '[anonymous]'}`), null) : (n.default.log(`SafetyCallback.find: 找不到 eventName-${t} 对应的 func`), null)
      }delete(t, e) {
        return 'function' === typeof e && (!!this._funcMap.has(t) && (!!this._funcMap.get(t).has(e) && (this._funcMap.get(t).delete(e), 0 === this._funcMap.get(t).size && this._funcMap.delete(t), !0)))
      }_pack(t, e, i) {
        return function() {
          try {
            e.apply(i, Array.from(arguments))
          } catch (e) {
            const i = Object.values(r.a).indexOf(t); const o = Object.keys(r.a)[i]; n.default.error(`接入侧事件 EVENT.${o} 对应的回调函数逻辑存在问题，请检查！`, e)
          }
        }
      }
    }
  }, function(t, e, i) {
    /**
 * UUID.js - RFC-compliant UUID Generator for JavaScript
 *
 * @file
 * @author  LiosK
 * @version v4.2.8
 * @license Apache License 2.0: Copyright (c) 2010-2021 LiosK
 */
    let n; n = function(e) {
      'use strict'; function n() {
        const t = o._getRandomInt; this.timestamp = 0, this.sequence = t(14), this.node = 1099511627776 * (1 | t(8)) + t(40), this.tick = t(4)
      } function o() {} return o.generate = function() {
        const t = o._getRandomInt; const e = o._hexAligner; return e(t(32), 8) + '-' + e(t(16), 4) + '-' + e(16384 | t(12), 4) + '-' + e(32768 | t(14), 4) + '-' + e(t(48), 12)
      }, o._getRandomInt = function(t) {
        if (t < 0 || t > 53) return NaN; const e = 0 | 1073741824 * Math.random(); return t > 30 ? e + 1073741824 * (0 | Math.random() * (1 << t - 30)) : e >>> 30 - t
      }, o._hexAligner = function(t, e) {
        for (var i = t.toString(16), n = e - i.length, o = '0'; n > 0; n >>>= 1, o += o)1 & n && (i = o + i); return i
      }, o.overwrittenUUID = e, function() {
        const t = o._getRandomInt; o.useMathRandom = function() {
          o._getRandomInt = t
        }; let e = null; let n = t; 'undefined' !== typeof window && (e = window.crypto || window.msCrypto) ? e.getRandomValues && 'undefined' !== typeof Uint32Array && (n = function(t) {
          if (t < 0 || t > 53) return NaN; let i = new Uint32Array(t > 32 ? 2 : 1); return i = e.getRandomValues(i) || i, t > 32 ? i[0] + 4294967296 * (i[1] >>> 64 - t) : i[0] >>> 32 - t
        }) : (e = i(13)) && e.randomBytes && (n = function(t) {
          if (t < 0 || t > 53) return NaN; const i = e.randomBytes(t > 32 ? 8 : 4); const n = i.readUInt32BE(0); return t > 32 ? n + 4294967296 * (i.readUInt32BE(4) >>> 64 - t) : n >>> 32 - t
        }), o._getRandomInt = n
      }(), o.FIELD_NAMES = ['timeLow', 'timeMid', 'timeHiAndVersion', 'clockSeqHiAndReserved', 'clockSeqLow', 'node'], o.FIELD_SIZES = [32, 16, 16, 8, 8, 48], o.genV4 = function() {
        const t = o._getRandomInt; return (new o)._init(t(32), t(16), 16384 | t(12), 128 | t(6), t(8), t(48))
      }, o.parse = function(t) {
        let e; if (e = /^\s*(urn:uuid:|\{)?([0-9a-f]{8})-([0-9a-f]{4})-([0-9a-f]{4})-([0-9a-f]{2})([0-9a-f]{2})-([0-9a-f]{12})(\})?\s*$/i.exec(t)) {
          const i = e[1] || ''; const n = e[8] || ''; if (i + n === '' || '{' === i && '}' === n || 'urn:uuid:' === i.toLowerCase() && '' === n) return (new o)._init(parseInt(e[2], 16), parseInt(e[3], 16), parseInt(e[4], 16), parseInt(e[5], 16), parseInt(e[6], 16), parseInt(e[7], 16))
        } return null
      }, o.prototype._init = function() {
        const t = o.FIELD_NAMES; const e = o.FIELD_SIZES; const i = o._binAligner; const n = o._hexAligner; this.intFields = new Array(6), this.bitFields = new Array(6), this.hexFields = new Array(6); for (let r = 0; r < 6; r++) {
          const s = parseInt(arguments[r] || 0); this.intFields[r] = this.intFields[t[r]] = s, this.bitFields[r] = this.bitFields[t[r]] = i(s, e[r]), this.hexFields[r] = this.hexFields[t[r]] = n(s, e[r] >>> 2)
        } return this.version = this.intFields.timeHiAndVersion >>> 12 & 15, this.bitString = this.bitFields.join(''), this.hexNoDelim = this.hexFields.join(''), this.hexString = this.hexFields[0] + '-' + this.hexFields[1] + '-' + this.hexFields[2] + '-' + this.hexFields[3] + this.hexFields[4] + '-' + this.hexFields[5], this.urn = 'urn:uuid:' + this.hexString, this
      }, o._binAligner = function(t, e) {
        for (var i = t.toString(2), n = e - i.length, o = '0'; n > 0; n >>>= 1, o += o)1 & n && (i = o + i); return i
      }, o.prototype.toString = function() {
        return this.hexString
      }, o.prototype.equals = function(t) {
        if (!(t instanceof o)) return !1; for (let e = 0; e < 6; e++) if (this.intFields[e] !== t.intFields[e]) return !1; return !0
      }, o.NIL = (new o)._init(0, 0, 0, 0, 0, 0), o.genV1 = function() {
        null == o._state && o.resetState(); const t = (new Date).getTime(); const e = o._state; t != e.timestamp ? (t < e.timestamp && e.sequence++, e.timestamp = t, e.tick = o._getRandomInt(4)) : Math.random() < o._tsRatio && e.tick < 9984 ? e.tick += 1 + o._getRandomInt(4) : e.sequence++; const i = o._getTimeFieldValues(e.timestamp); const n = i.low + e.tick; const r = 4095 & i.hi | 4096; e.sequence &= 16383; const s = e.sequence >>> 8 | 128; const a = 255 & e.sequence; return (new o)._init(n, i.mid, r, s, a, e.node)
      }, o.resetState = function() {
        o._state = new n
      }, o._tsRatio = 1 / 4, o._state = null, o._getTimeFieldValues = function(t) {
        const e = t - Date.UTC(1582, 9, 15); const i = e / 4294967296 * 1e4 & 268435455; return { low: 1e4 * (268435455 & e) % 4294967296, mid: 65535 & i, hi: i >>> 16, timestamp: e }
      }, 'object' === typeof t.exports && (t.exports = o), o
    }(n)
  }, function(t, e) {}, function(e, i) {
    e.exports = t
  }, function(t) {
    t.exports = JSON.parse('{"name":"tsignaling","version":"0.7.0","description":"腾讯云 Web 信令 SDK","main":"./src/index.ts","scripts":{"lint":"./node_modules/.bin/eslint ./src","fix":"./node_modules/.bin/eslint --fix ./src","ts2js":"tsc src/index.ts --outDir build/ts2js","doc":"npm run ts2js && npm run doc:clean && npm run doc:build","doc:build":"./node_modules/.bin/jsdoc -c build/jsdoc/jsdoc.json && node ./build/jsdoc/fix-doc.js","doc:clean":"node ./build/jsdoc/clean-doc.js","build:wx":"cross-env NODE_ENV=wx webpack --config webpack.prod.config.js","build:web":"node node_modules/cross-env/src/bin/cross-env.js NODE_ENV=web node_modules/webpack/bin/webpack.js --config webpack.prod.config.js","build:package":"node build/package-bundle.js","prerelease":"npm run build:web && npm run build:wx && npm run build:package && node ./build/copy.js","start:wx":"cross-env NODE_ENV=wx  webpack-dev-server --config webpack.config.js","start:web":"node node_modules/cross-env/src/bin/cross-env.js NODE_ENV=web node_modules/webpack-dev-server/bin/webpack-dev-server.js --config webpack.dev.config.js","build_withcopy":"npm run build:web && cp dist/npm/tsignaling-js.js ../TIM-demo-web/node_modules/tsignaling/tsignaling-js.js","build_withcopy:mp":"npm run build:wx && cp dist/npm/tsignaling-wx.js ../TIM-demo-mini/static/component/TRTCCalling/utils/tsignaling-wx.js","changelog":"conventional-changelog -p angular -i CHANGELOG.md -s -r 0"},"husky":{"hooks":{"pre-commit":"npm run lint"}},"lint-staged":{"*.{.ts,.tsx}":["eslint","git add"]},"keywords":["腾讯云","即时通信","信令"],"author":"","license":"ISC","devDependencies":{"conventional-changelog-cli":"^2.1.1","cross-env":"^7.0.2","fs-extra":"^9.0.1","html-webpack-plugin":"^4.3.0","ts-loader":"^7.0.5","typescript":"^3.9.9","webpack":"^4.43.0","webpack-cli":"^3.3.11","webpack-dev-server":"^3.11.0"},"dependencies":{"@typescript-eslint/eslint-plugin":"^4.22.1","@typescript-eslint/parser":"^4.22.1","EventEmitter":"^1.0.0","docdash-blue":"^1.1.3","eslint":"^5.16.0","eslint-config-google":"^0.13.0","eslint-plugin-classes":"^0.1.1","jsdoc":"^3.6.4","jsdoc-plugin-typescript":"^2.0.5","pretty":"^2.0.0","replace":"^1.2.0","uuidjs":"^4.2.5"}}')
  }]).default
}))
