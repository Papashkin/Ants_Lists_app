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
            val filesPath = (mContext.applicationInfo.dataDir) + "/files/"
            files = File(filesPath).listFiles()
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
            listNames = arrayListOf()
            files.forEach {
                listNames.add(it.nameWithoutExtension)
            }
        }

        override fun onCreate() {
            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun onDataSetChanged() {
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getViewAt(position: Int): RemoteViews {
            val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
            rv.setTextViewText(R.id.widget_item, listNames[position])

            val extras = Bundle()
            extras.putInt(AntsWidget.EXTRA_ITEM, position)
            extras.putString("LIST_NAME", listNames[position])

            val fillInIntent = Intent()
            fillInIntent.putExtras(extras)
            rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent)
            try {
                System.out.println("Loading view $position")
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

    }
}