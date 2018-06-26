package com.example.ankur.demoproject

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.View
import android.webkit.DownloadListener
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ProgressBar
import java.io.*
import java.net.URI
import java.net.URL


class MainActivity : AppCompatActivity() {

    val URL_PATH="https://github.com/MapleGraph/apkfile/blob/master/com.google.android.gms_11.9.51_(234-177350961)-11951234_minAPI21(armeabi-v7a)(240dpi)_apkmirror.com.apk?raw=true"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()
    }

     val PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)


    internal fun checkPermission() {
        if (!hasPermissions(this@MainActivity, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this@MainActivity, PERMISSIONS, 1)
        } else {
            downloadAPKTask().execute()
        }
    }

    fun hasPermissions(context: Context?, permissions:Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadAPKTask().execute()
            }

            else -> {
            }
        }
    }


    val DIRPATH = "/mnt/sdcard/Download/"
    val appName="Demo"


    public fun installDemoApkFromUrl(){


        var url:URL = URL(URL_PATH)

        val connection = url.openConnection()
        connection.connect()

        val file:File= File(DIRPATH)
        file.mkdirs();
        val outputFile = File(file, appName + ".apk")
        if (outputFile.exists()) {
            outputFile.delete()
        }


        val lengthOfFile = connection.contentLength
        Log.i("LENGTHOFDATA", "" + lengthOfFile)


        val input = BufferedInputStream(url.openStream())
        val output = FileOutputStream(outputFile)

        val data = ByteArray(1024)

        var total: Long = 0

        var count: Int=input.read(data)



        while (count != -1) {
            total += count.toLong()
            val progressValue = (total * 100 / lengthOfFile).toInt()

            output.write(data, 0, count)
            count=input.read(data);
        }

        output.flush()
        output.close()
        input.close()

    }


    internal inner class downloadAPKTask : AsyncTask<Void, Void, Boolean>() {
        override fun onPreExecute() {
            super.onPreExecute()

            //progressBar = ProgressBar(this@MainActivity, null, android.R.attr.progressBarStyleLarge)

            progressDialog= ProgressDialog(this@MainActivity);
            progressDialog.setTitle("Login");
            progressDialog.setMessage("LogIn in Progress")
            progressDialog.show()

        }

        override fun doInBackground(vararg params: Void): Boolean? {
            try {
                installDemoApkFromUrl()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return false
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)

              if (progressDialog!= null && progressDialog.isShowing)
                  progressDialog.dismiss()


            val intent = Intent(Intent.ACTION_VIEW)
            //intent.setDataAndType(Uri.fromFile(File(DIRPATH + appName + ".apk")), "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK // without this flag android returned a intent error!
            var apkURI:Uri=FileProvider.getUriForFile(this@MainActivity,applicationContext.packageName+".provider",File(DIRPATH + appName + ".apk"))
            intent.setDataAndType(apkURI,"application/vnd.android.package-archive")
            intent.flags=Intent.FLAG_GRANT_READ_URI_PERMISSION;
            startActivity(intent)



        }
    }

    lateinit var progressDialog:ProgressDialog;

}
