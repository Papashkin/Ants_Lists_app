package com.papashkin.shoppingantlist

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.widget.RemoteViews
import android.widget.Toast
import java.io.File

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AntsWidget: AppWidgetProvider() {
    lateinit var listNames: ArrayList<String>

    companion object {
        val TOAST_ACTION = "com.papashkin.shoppingantlist.TOAST_ACTION"
        val EXTRA_ITEM = "com.papashkin.shoppingantlist.EXTRA_ITEM"
    }

    override fun onEnabled(context: Context) {
        checFiles(context)
        super.onEnabled(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val mgr = AppWidgetManager.getInstance(context)
        if (intent.action == TOAST_ACTION) {
            checFiles(context)
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID)
            val viewIndex = intent.getIntExtra(EXTRA_ITEM, 0)
            val listName = listNames[viewIndex]
            Toast.makeText(context, "Touched view $viewIndex", Toast.LENGTH_SHORT).show()

            val appIntent = Intent(context, UseList::class.java)
            val testIntent = PendingIntent.getActivity(
                    context,1, appIntent, 0)
            appIntent.putExtra("FROM_WIDGET", true)
            appIntent.putExtra("LIST_NAME", listName)
            val widgetBundle = Bundle()
            widgetBundle.putBoolean("FROM_WIDGET", true)
            if (listName != null) {
                widgetBundle.putString("LIST_NAME", listName)
            }
            appIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(context, appIntent, widgetBundle)
//            context.startActivity(appIntent, widgetBundle)
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager,
                          appWidgetIds: IntArray?) {
        checFiles(context)
        for (i in 0 until appWidgetIds!!.size) {
            val intent = Intent(context, StackService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i])
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

            val rv = RemoteViews(context.packageName, R.layout.widget_layout)
            rv.setRemoteAdapter(R.id.widget_listview, intent)

//            val filesPath = (context.applicationInfo.dataDir) + "/files/"
//            if (File(filesPath).listFiles().isEmpty()){
////                rv.setEmptyView(R.id.empty_view,R.id.empty_view)
//            }

            val toastIntent = Intent(context, AntsWidget::class.java)
//            val toastIntent = Intent(context, UseList::class.java)
//            toastIntent.setClassName("com.papashkin.shoppingantlist",
//                    "com.papashkin.shoppingantlist.UseList")
            toastIntent.action = AntsWidget.TOAST_ACTION
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i])
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

//            val listName = listNames[i]
//            val appIntent = Intent(context, UseList::class.java)
//            appIntent.putExtra("LIST_NAME", listName)
//            val appPendingIntent = PendingIntent.getActivity(context, 0,
//                    appIntent, 0)
//            rv.setOnClickPendingIntent(R.id.widget_listview, appPendingIntent)

            val toastPendingIntent = PendingIntent.getBroadcast(
                    context, 0, toastIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setPendingIntentTemplate(R.id.widget_listview, toastPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
    }

    private fun checFiles(context: Context){
        val filesPath = (context.applicationInfo.dataDir) + "/files/"
        val files = File(filesPath).listFiles()
        listNames = arrayListOf()
        files.forEach {
            listNames.add(it.nameWithoutExtension)
        }
    }
}