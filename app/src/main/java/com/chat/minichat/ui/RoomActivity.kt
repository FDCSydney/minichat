package com.chat.minichat.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.chat.minichat.R
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalAudioStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.LocalRoomMember
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.P2PRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class RoomActivity : AppCompatActivity() {
    private val option = SkyWayContext.Options(
        authToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIyYTczNTllMC1kNjNkLTQ4YTgtODk2Ni01NTQ3YTYzMzkwNjkiLCJpYXQiOjE2OTMyOTgxNzMsImV4cCI6MTY5MzM4NDU3Mywic2NvcGUiOnsiYXBwIjp7ImlkIjoiMmVkMGFkNjMtODE4OC00N2U1LTljZmEtYTdiN2FlZDg2Zjk2IiwidHVybiI6dHJ1ZSwiYWN0aW9ucyI6WyJyZWFkIl0sImNoYW5uZWxzIjpbeyJpZCI6IioiLCJuYW1lIjoiKiIsImFjdGlvbnMiOlsid3JpdGUiXSwibWVtYmVycyI6W3siaWQiOiIqIiwibmFtZSI6IioiLCJhY3Rpb25zIjpbIndyaXRlIl0sInB1YmxpY2F0aW9uIjp7ImFjdGlvbnMiOlsid3JpdGUiXX0sInN1YnNjcmlwdGlvbiI6eyJhY3Rpb25zIjpbIndyaXRlIl19fV0sInNmdUJvdHMiOlt7ImFjdGlvbnMiOlsid3JpdGUiXSwiZm9yd2FyZGluZ3MiOlt7ImFjdGlvbnMiOlsid3JpdGUiXX1dfV19XX19fQ.AiZeeey5UarMxgwQlwYOYesHouez5U6Ng9hCEjl4jSQ",
        logLevel = Logger.LogLevel.VERBOSE
    )

    private val scope = CoroutineScope(Dispatchers.IO)
    private var localRoomMember     : LocalRoomMember?  = null
    private var room                : P2PRoom?          = null
    private var localVideoStream    : LocalVideoStream? = null
    private var localAudioStream    : LocalAudioStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        initUI();
    }

    private fun initUI(){
        val name = intent.getStringExtra("roomID")
        checkAndRequestPermissions();
        if (name != null) {
            joinAndPublish(name)
        };
    }

    private fun checkAndRequestPermissions(){
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.CAMERA
            ) != PermissionChecker.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.RECORD_AUDIO
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO
                ),
                0
            )
        }
    }

    private fun joinAndPublish(roomName: String){
        scope.launch() {
            val result = SkyWayContext.setup(applicationContext, option)
            if (result) {
                Log.d("App", "Setup succeed")
            }

            val device = CameraSource.getFrontCameras(applicationContext).first()

            val cameraOption = CameraSource.CapturingOptions(800, 800)
            CameraSource.startCapturing(applicationContext, device, cameraOption)

            localVideoStream = CameraSource.createStream()

            runOnUiThread {
                val localVideoRenderer = findViewById<SurfaceViewRenderer>(R.id.local_renderer)
                localVideoRenderer.setup()
                localVideoStream!!.addRenderer(localVideoRenderer)
            }

            AudioSource.start()

            localAudioStream = AudioSource.createStream()
            room = P2PRoom.findOrCreate(name = roomName)
            val memberInit = RoomMember.Init(name = "member_" + UUID.randomUUID())
            localRoomMember = room?.join(memberInit)

            val resultMessage = if (localRoomMember == null) "Join failed" else "Joined room"
            runOnUiThread {
                Toast.makeText(applicationContext, resultMessage, Toast.LENGTH_SHORT)
                    .show()
            }

            room?.publications?.forEach {
                if (it.publisher?.id == localRoomMember?.id) return@forEach
                subscribe(it)
            }

            room?.onStreamPublishedHandler = Any@{
                Log.d("room", "onStreamPublished: ${it.id}")
                if (it.publisher?.id == localRoomMember?.id) {
                    return@Any
                }
                subscribe(it)
            }

            localRoomMember?.publish(localVideoStream!!)
            localRoomMember?.publish(localAudioStream!!)
        }
    }
    private fun subscribe(publication: RoomPublication) {
        scope.launch {
            val subscription = localRoomMember?.subscribe(publication)
            runOnUiThread {
                val remoteVideoRenderer =
                    findViewById<SurfaceViewRenderer>(R.id.remote_renderer)
                remoteVideoRenderer.setup()
                val remoteStream = subscription?.stream
                when (remoteStream?.contentType) {
                    Stream.ContentType.VIDEO -> (remoteStream as RemoteVideoStream).addRenderer(
                        remoteVideoRenderer
                    )
                    else -> {}
                }
            }
        }
    }
}