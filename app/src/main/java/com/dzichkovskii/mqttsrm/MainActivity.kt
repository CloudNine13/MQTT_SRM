package com.dzichkovskii.mqttsrm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.dzichkovskii.mqttsrm.fragments.ConnectFragment
import com.dzichkovskii.mqttsrm.fragments.PublishFragment
import com.dzichkovskii.mqttsrm.fragments.SubscribeFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ConnectFragment()).commit()

        navView.setOnNavigationItemSelectedListener(navListener)
    }

    private val navListener : BottomNavigationView.OnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            var selectedFragment : Fragment? = null

            when(item.itemId) {
                R.id.fragment_connect -> selectedFragment = ConnectFragment()
                R.id.fragment_subscribe -> selectedFragment = SubscribeFragment()
                R.id.fragment_publish -> selectedFragment = PublishFragment()
            }
            //Changing to the chosen item
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment!!).commit()

            true
        }
}
