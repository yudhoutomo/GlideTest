package com.test.glidetest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.Excludes
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.Option
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.module.AppGlideModule
import java.io.InputStream

@GlideModule
@Excludes(OkHttpLibraryGlideModule::class)
class MyGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        //val session = CoreSession(context)
        var client = UnsafeOkHttpClient().unsafeOkHttpClient

        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(client)
        )
        registry.prepend(InputStream::class.java, Bitmap::class.java, PageDecoder(glide.bitmapPool))
    }

    class PageDecoder(private val bitmapPool: BitmapPool) : ResourceDecoder<InputStream, Bitmap> {

        companion object {
            val PAGE_DECODER: Option<Boolean> = Option.memory("abc")
        }

        override fun decode(
            source: InputStream,
            width: Int,
            height: Int,
            options: Options
        ): Resource<Bitmap>? {
            return BitmapResource.obtain(BitmapFactory.decodeStream(source), bitmapPool)
        }

        override fun handles(source: InputStream, options: Options): Boolean =
            options.get(PAGE_DECODER) ?: false

    }
}