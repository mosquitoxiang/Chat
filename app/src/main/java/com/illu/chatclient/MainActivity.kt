package com.illu.chatclient

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.permissionx.guolindev.PermissionX
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.filter.Filter
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var adapter: MsgAdapter
    private var msgList = mutableListOf<Msg>()
    private var mSelected = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)
        btnBind.setOnClickListener(this)
        btnConnect.setOnClickListener(this)
        btnSend.setOnClickListener(this)
        imgSelect.setOnClickListener(this)
        msgList = ArrayList()

        adapter = MsgAdapter().apply {
            bindToRecyclerView(rv)
        }
        //连接服务器
//        thread {
//            Client("192.168.31.240", 8899).start()
//        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.btnBind -> {
                val bindId = etSelfId.text.toString().trim()
                if (bindId.isNotEmpty()) {
                    Client().bind(bindId)
                }
            }
            R.id.btnConnect -> {
                val connectId = etConnectId.text.toString().trim()
                if (connectId.isNotEmpty()) {
                    Client().connect(connectId)
                }
            }
            R.id.btnSend -> {
                val connectId = etConnectId.text.toString().trim()
                val msg = etContent.text.toString().trim()
                if (msg.isNotEmpty() && connectId.isNotEmpty()) {
                    Client().sendMsg(connectId, msg, object : ResultCallback{
                        override fun send(isSuccess: Boolean) {
                            val msgObj = Msg(msg, Msg.TYPE_SEND)
                            msgList.add(msgObj)
                            adapter.setNewData(msgList)
                            rv.smoothScrollToPosition(msgList.size-1)
                        }
                    })
                }
            }
            R.id.imgSelect -> {
                PermissionX.init(this)
                    .permissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                     android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .request { allGranted, _, _ ->
                        if (allGranted) {
                            Matisse.from(this)
                                .choose(MimeType.ofAll())
                                .countable(true)
                                .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                                .maxSelectable(9)
                                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                .thumbnailScale(0.85f)
                                .imageEngine(GlideEngine())
                                .showPreview(true)
                                .forResult(REQUEST_CODE_CHOOSE);
                        }
                    }
            }
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK) {
            mSelected = Matisse.obtainResult(data)
            mSelected.forEach {
                val msgObj = Msg(type = Msg.TYPE_SEND, picUri = it)
                msgList.add(msgObj)
            }
            val imageAbsolutePath = UriToPathUtil.getImageAbsolutePath(this, msgList[0].picUri)
            adapter.setNewData(msgList)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(msg: String) {
        val msgObj = Msg(msg, Msg.TYPE_RECEIVE)
        msgList.add(msgObj)
        adapter.setNewData(msgList)
        rv.smoothScrollToPosition(msgList.size-1)
    }

    companion object {
        const val REQUEST_CODE_CHOOSE = 2000
    }
}