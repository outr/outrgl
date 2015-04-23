package com.outr.gl.download

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net.{HttpResponse, HttpResponseListener, HttpMethods}
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.net.HttpRequestBuilder
import com.badlogic.gdx.utils.async.AsyncTask

/**
 * @author Matt Hicks <matt@outr.com>
 */
class DownloadTask(manager: DownloadManager, url: String, local: FileHandle) extends AsyncTask[FileHandle] {
  override def call() = {
    if (!local.exists()) {
      val request = new HttpRequestBuilder().newRequest().method(HttpMethods.GET).url(url).build()
      var finished = false
      Gdx.net.sendHttpRequest(request, new HttpResponseListener {
        override def handleHttpResponse(httpResponse: HttpResponse) = {
          local.writeBytes(httpResponse.getResult, false)
          finished = true
        }

        override def cancelled() = {}

        override def failed(t: Throwable) = {
          t.printStackTrace()
        }
      })
      while (!finished) {
        Thread.sleep(50)
      }
    }
    manager.finished(this)
    local
  }
}