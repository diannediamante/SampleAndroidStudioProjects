package com.example.samplenfcapp

import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.samplenfcapp.MainViewModel
import com.example.samplenfcapp.NFCManager
import com.example.samplenfcapp.databinding.ActivityBinder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity, CompoundButton.OnCheckedChangeListener, NfcAdapter.ReaderCallback {

    companion object {
        private val TAG = MainActivity::class.java.getSimpleName()
    }

    private var binder : ActivityBinder? = null
    private val viewModel : MainViewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    constructor() {

    }

    override fun onCreate(savedInstanceState : Bundle?) {
        binder = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)
        binder?.setViewModel(viewModel)
        binder?.setLifecycleOwner(this@MainActivity)
        super.onCreate(savedInstanceState)
        binder?.toggleButton?.setOnCheckedChangeListener(this@MainActivity)
        Coroutines.main(this@MainActivity, { scope ->
            scope.launch( block = { binder?.getViewModel()?.observeNFCStatus()?.collectLatest ( action = { status -> Log.d(TAG, "observeNFCStatus $status")
                if (status == NFCStatus.NoOperation) NFCManager.disableReaderMode(this@MainActivity, this@MainActivity)
                else if (status == NFCStatus.Tap) NFCManager.enableReaderMode(this@MainActivity, this@MainActivity, this@MainActivity, viewModel.getNFCFlags(), viewModel.getExtras())
            }) })
            scope.launch( block = { binder?.getViewModel()?.observeToast()?.collectLatest ( action = { message -> Log.d(TAG, "observeToast $message")
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            }) })
            scope.launch( block = { binder?.getViewModel()?.observeTag()?.collectLatest ( action = { tag -> Log.d(TAG, "observeTag $tag")
                binder?.textViewExplanation?.setText(tag)
            }) })
        })
    }

    override fun onCheckedChanged(buttonView : CompoundButton?, isChecked : Boolean) {
        if (buttonView == binder?.toggleButton)
            viewModel.onCheckNFC(isChecked)
    }

    override fun onTagDiscovered(tag : Tag?) {
        binder?.getViewModel()?.readTag(tag)
    }
}