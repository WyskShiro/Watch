package will.shiro.watch

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import will.shiro.watch.databinding.ActivityMainBinding


private const val CLIENT_ID = "152c10d5a27141b189150277313335e3"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var spotifyApp: SpotifyAppRemote
    private var playerState: PlayerState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        hideActionBar()
        setupSpotifyIntegration()
    }

    private fun setupSpotifyIntegration() {
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri("google://com")
            .showAuthView(true)
            .build()
        SpotifyAppRemote.connect(this, connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    onSpotifyConnectSuccess(spotifyAppRemote)
                }

                override fun onFailure(throwable: Throwable) {
                    onSpotifyConnectFailure(throwable)
                }
            })
    }

    private fun onSpotifyConnectSuccess(spotifyAppRemote: SpotifyAppRemote) {
        Log.d("MainActivity", "Connected! Yay!")
        spotifyApp = spotifyAppRemote
        spotifyApp.playerApi.subscribeToPlayerState().setEventCallback {
            playerState = it
        }
        binding.back.setOnClickListener {
            spotifyApp.playerApi.skipPrevious()
        }
        binding.next.setOnClickListener {
            spotifyApp.playerApi.skipNext()
        }
        binding.pauseOrContinue.setOnClickListener {
            val view = it as ImageButton
            playerState?.run {
                if (isPaused) {
                    spotifyApp.playerApi.resume()
                    view.setImageResource(R.drawable.ic_round_pause)
                }
                else {
                    spotifyApp.playerApi.pause()
                    view.setImageResource(R.drawable.ic_round_play)
                }
            }
        }
    }

    private fun onSpotifyConnectFailure(throwable: Throwable) {
        Log.e("MainActivity", throwable.message, throwable)
    }

    private fun hideActionBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}