/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.android.gui;

import android.app.Application;
import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.view.ViewConfiguration;
import com.andrew.apollo.cache.ImageCache;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.CoreRuntimeException;
import com.frostwire.android.core.SystemPaths;
import com.frostwire.android.gui.services.Engine;
import com.frostwire.android.util.HttpResponseCache;
import com.frostwire.android.util.ImageLoader;
import com.frostwire.bittorrent.BTContext;
import com.frostwire.bittorrent.BTEngine;
import com.frostwire.logging.Logger;
import com.frostwire.search.CrawlPagedWebSearchPerformer;
import com.frostwire.util.DirectoryUtils;
import org.gudy.azureus2.core3.util.protocol.AzURLStreamHandlerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;

/**
 * @author gubatron
 * @author aldenml
 */
public class MainApplication extends Application {

    private static final Logger LOG = Logger.getLogger(MainApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();

        try {

            ignoreHardwareMenu();
            installHttpCache();

            ConfigurationManager.create(this);

            setupBTEngine();

            NetworkManager.create(this);
            Librarian.create(this);
            Engine.create(this);

            ImageLoader.getInstance(this);
            CrawlPagedWebSearchPerformer.setCache(new DiskCrawlCache(this));
            CrawlPagedWebSearchPerformer.setMagnetDownloader(new LibTorrentMagnetDownloader());

            LocalSearchEngine.create(getDeviceId());//getAndroidId());

            cleanTemp();

            Librarian.instance().syncMediaStore();
            Librarian.instance().syncApplicationsProvider();
        } catch (Throwable e) {
            throw new CoreRuntimeException("Unable to initialized main components", e);
        }
    }

    private String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();

        //probably it's a tablet... Sony's tablet returns null here.
        if (deviceId == null) {
            deviceId = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
        }
        return deviceId;
    }

    @Override
    public void onLowMemory() {
        ImageCache.getInstance(this).evictAll();
        ImageLoader.getInstance(this).clear();
        super.onLowMemory();
    }

    private void ignoreHardwareMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field f = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (f != null) {
                f.setAccessible(true);
                f.setBoolean(config, false);
            }
        } catch (Throwable e) {
            // ignore
        }
    }

    private void installHttpCache() {
        try {
            HttpResponseCache.install(this);
        } catch (IOException e) {
            LOG.error("Unable to install global http cache", e);
        }
    }

    private void setupBTEngine() {
        // this hack is only due to the remaining vuze TOTorrent code
        URL.setURLStreamHandlerFactory(new AzURLStreamHandlerFactory());

        BTEngine.ctx = new BTContext();
        BTEngine.getInstance().reloadBTContext(SystemPaths.getTorrents(),
                SystemPaths.getTorrentData(),
                SystemPaths.getLibTorrent(this),
                0,0,"0.0.0.0",false,false);
        BTEngine.ctx.optimizeMemory = true;
        BTEngine.getInstance().start();
    }

    private void cleanTemp() {
        try {
            File tmp = SystemPaths.getTemp();
            DirectoryUtils.deleteFolderRecursively(tmp);

            if (tmp.mkdirs()) {
                new File(tmp, ".nomedia").createNewFile();
            }
        } catch (Throwable e) {
            LOG.error("Error during setup of temp directory", e);
        }
    }
}
