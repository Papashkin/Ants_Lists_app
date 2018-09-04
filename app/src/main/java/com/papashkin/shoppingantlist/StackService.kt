package com.papashkin.shoppingantlist

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import java.io.File

class StackService: RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return StackRemoteViewsFactory(this.applicationContext, intent!!)
    }

    class StackRemoteViewsFactory(context: Context, intent: Intent): RemoteViewsService.RemoteViewsFactory{
        private var mContext: Context = context
        private var mAppWidgetId: Int
        private var files: Array<File>
        private var listNames: ArrayList<String>

        init {
            files = arrayOf()
            listNames = arrayListOf()
            checkFiles(mContext)
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        override fun onCreate() {
            checkFiles(mContext)
            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        override fun getLoadingView(): RemoteViews? {
            checkFiles(mContext)
            return null
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun onDataSetChanged() {
            checkFiles(mContext)
            for (i in listNames.indices){
                getViewAt(i)
            }
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getViewAt(position: Int): RemoteViews {
            val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
            rv.setTextViewText(R.id.widget_item, listNames[position])

            val extras = Bundle()
            extras.putInt(AntsWidget.EXTRA_ITEM, position)

            val fillInIntent = Intent()
            fillInIntent.putExtras(extras)
            rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent)
            try {
                System.out.println("Loading view $position: list ${listNames[position]}")
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return rv
        }

        override fun getCount(): Int {
            return listNames.size
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun onDestroy() {
            listNames.clear()
        }

        private fun checkFiles(context: Context){
            val filesPath = (context.applicationInfo.dataDir) + "/files/"
            val files = File(filesPath).listFiles()
            listNames = arrayListOf()
            files.forEach {
                listNames.add(it.nameWithoutExtension)
            }
        }
    }
}