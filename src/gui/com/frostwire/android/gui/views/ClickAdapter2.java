/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
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

package com.frostwire.android.gui.views;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;

import com.frostwire.util.Ref;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public abstract class ClickAdapter2<T extends Context> implements View.OnClickListener, View.OnLongClickListener, View.OnKeyListener, DialogInterface.OnClickListener, CompoundButton.OnCheckedChangeListener {

    protected final WeakReference<T> ctxRef;

    public ClickAdapter2(T ctx) {
        this.ctxRef = Ref.weak(ctx);
    }

    @Override
    public final void onClick(View v) {
        if (Ref.alive(ctxRef)) {
            onClick(ctxRef.get(), v);
        }
    }

    @Override
    public final boolean onLongClick(View v) {
        if (Ref.alive(ctxRef)) {
            return onLongClick(ctxRef.get(), v);
        }

        return false;
    }

    @Override
    public final boolean onKey(View v, int keyCode, KeyEvent event) {
        if (Ref.alive(ctxRef)) {
            return onKey(ctxRef.get(), v, keyCode, event);
        }

        return false;
    }

    @Override
    public final void onClick(DialogInterface dialog, int which) {
        if (Ref.alive(ctxRef)) {
            onClick(ctxRef.get(), dialog, which);
        }
    }

    @Override
    public final void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (Ref.alive(ctxRef)) {
            onCheckedChanged(ctxRef.get(), buttonView, isChecked);
        }
    }

    public void onClick(T ctx, View v) {
    }

    public boolean onLongClick(T ctx, View v) {
        return false;
    }

    public boolean onKey(T ctx, View v, int keyCode, KeyEvent event) {
        return false;
    }

    public void onClick(T ctx, DialogInterface dialog, int which) {
    }

    public void onCheckedChanged(T ctx, CompoundButton buttonView, boolean isChecked) {
    }
}
