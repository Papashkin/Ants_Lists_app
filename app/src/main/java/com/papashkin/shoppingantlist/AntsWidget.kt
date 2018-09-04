package com.papashkin.shoppingantlist

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.content.ComponentName
import android.net.Uri
import android.widget.RemoteViews
import java.io.File

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AntsWidget: AppWidgetProvider() {
    private lateinit var listNames: ArrayList<String>
    lateinit var filesPath: String

    companion object {
        const val EXTRA_ITEM = "com.papashkin.shoppingantlist.EXTRA_ITEM"
    }

    override fun onEnabled(context: Context) {
        checkFiles(context)
        val mgr = AppWidgetManager.getInstance(context)
        val cn = ComponentName(context, AntsWidget::class.java)
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.widget_listview)
        super.onEnabled(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE){
            checkFiles(context)
//            (Toast.makeText(context, "refresh ...", Toast.LENGTH_SHORT)).show()
            val mgr = AppWidgetManager.getInstance(context)
            val cn = ComponentName(context, AntsWidget::class.java)
            val ids = mgr.getAppWidgetIds(cn)
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.widget_listview)
            onUpdate(context, mgr, ids)
        }

        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_BIND) {
            checkFiles(context)
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID)
            val viewIndex = intent.getIntExtra(EXTRA_ITEM, 0)
            val listName = listNames[viewIndex]

            val appIntent = Intent(context, UseList::class.java)
            appIntent.putExtra("FROM_WIDGET", true)
            appIntent.putExtra("LIST_NAME", listName)
            appIntent.putExtra("LIST", readFile(listName))
            appIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            context.startActivity(appIntent)
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager,
                          appWidgetIds: IntArray?) {
        checkFiles(context)
        val flag = Intent.FLAG_ACTIVITY_NEW_TASK
        for (i in 0 until appWidgetIds!!.size) {
            val awmID = appWidgetIds[i]
            val intent = Intent(context, StackService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, awmID)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

            val rv = RemoteViews(context.packageName, R.layout.widget_layout)
            rv.setRemoteAdapter(R.id.widget_listview, intent)

            // intent for listview item
            val actIntent = Intent(context, AntsWidget::class.java)
            actIntent.action = AppWidgetManager.ACTION_APPWIDGET_BIND
            actIntent.addCategory("android.intent.category.LAUNCHER")
            actIntent.flags = flag
            actIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, awmID)

            val actPendingIntent = PendingIntent.getBroadcast(
                    context, 0, actIntent, 0)
            rv.setPendingIntentTemplate(R.id.widget_listview, actPendingIntent)

            // intent for imageButton
            val updIntent = Intent(context, AntsWidget::class.java)
            updIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            updIntent.flags = flag
            updIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)

            val updPendingIntent = PendingIntent.getBroadcast(
                    context, 0, updIntent, 0)
            rv.setOnClickPendingIntent(R.id.button_refresh, updPendingIntent)

            appWidgetManager.updateAppWidget(awmID, rv)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
    }

    private fun checkFiles(context: Context){
        filesPath = (context.applicationInfo.dataDir) + "/files/"
        val files = File(filesPath).listFiles()
        listNames = arrayListOf()
        files.forEach {
            listNames.add(it.nameWithoutExtension)
        }
    }

    private fun readFile(fileName: String): ArrayList<String>{
        val list = arrayListOf<String>()
        val file = File(filesPath, "$fileName.txt")
        file.useLines { lines -> lines.forEach{
            if (it != "") list.add(it)
        }
        }
        return list
    }

    fun sendRefreshBroadcast(context: Context){
        val updIntent = Intent(context, AntsWidget::class.java)
        updIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        onReceive(context, updIntent)
    }
}