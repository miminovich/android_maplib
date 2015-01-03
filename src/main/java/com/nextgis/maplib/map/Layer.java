/******************************************************************************
 * Project:  NextGIS mobile
 * Purpose:  Mobile GIS for Android.
 * Author:   Dmitry Baryshnikov (aka Bishop), polimax@mail.ru
 ******************************************************************************
 *   Copyright (C) 2014 NextGIS
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ****************************************************************************/
package com.nextgis.maplib.map;

import android.content.Context;
import com.nextgis.maplib.api.IJSONStore;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.ILayerView;
import com.nextgis.maplib.api.IRenderer;
import com.nextgis.maplib.datasource.GeoEnvelope;
import com.nextgis.maplib.display.GISDisplay;
import com.nextgis.maplib.util.FileUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import static com.nextgis.maplib.util.Constants.*;
import static com.nextgis.maplib.util.GeoConstants.DEFAULT_MAX_ZOOM;
import static com.nextgis.maplib.util.GeoConstants.DEFAULT_MIN_ZOOM;


public class Layer
        implements ILayer, ILayerView, IJSONStore, IRenderer
{
    protected String      mName;
    protected boolean     mIsVisible;
    protected short       mId;
    protected float       mMaxZoom;
    protected float       mMinZoom;
    protected File        mPath;
    protected int         mLayerType;
    protected IRenderer   mRenderer;
    protected GeoEnvelope mExtents;
    protected ILayer      mParent;
    protected Context     mContext;


    public Layer(
            Context context,
            File path)
    {
        mPath = path;
        mContext = context;
    }


    @Override
    public void runDraw(GISDisplay display)
    {
        if (mRenderer != null) {
            mRenderer.runDraw(display);
        }
    }


    @Override
    public String getName()
    {
        return mName;
    }


    @Override
    public void cancelDraw()
    {
        if (mRenderer != null) {
            mRenderer.cancelDraw();
        }
    }

    @Override
    public void setName(String newName)
    {
        this.mName = newName;
        if (mParent != null && mParent instanceof LayerGroup) {
            LayerGroup group = (LayerGroup) mParent;
            group.onLayerChanged(this);
        }
    }


    @Override
    public short getId()
    {
        return mId;
    }


    @Override
    public int getType()
    {
        return mLayerType;
    }


    @Override
    public boolean isVisible()
    {
        return mIsVisible;
    }


    @Override
    public void setVisible(boolean visible)
    {
        mIsVisible = visible;
        if (mParent != null && mParent instanceof LayerGroup) {
            LayerGroup group = (LayerGroup) mParent;
            group.onLayerChanged(this);
        }
    }


    @Override
    public boolean delete()
    {
        FileUtil.deleteRecursive(mPath);
        if (mParent != null && mParent instanceof LayerGroup) {
            LayerGroup group = (LayerGroup) mParent;
            group.onLayerDeleted(mId);
        }
        return true;
    }


    @Override
    public float getMaxZoom()
    {
        return mMaxZoom;
    }


    @Override
    public void setMaxZoom(float maxZoom)
    {
        mMaxZoom = maxZoom;
    }


    @Override
    public float getMinZoom()
    {
        return mMinZoom;
    }


    @Override
    public void setMinZoom(float minZoom)
    {
        mMinZoom = minZoom;
    }


    @Override
    public File getPath()
    {
        return mPath;
    }


    @Override
    public boolean save()
    {
        try {
            FileUtil.createDir(getPath());
            File config_file = new File(getPath(), LAYER_CONFIG);
            FileUtil.writeToFile(config_file, toJSON().toString());
        } catch (IOException e) {
            return false;
        } catch (JSONException e) {
            return false;
        }
        return true;
    }


    @Override
    public boolean load()
    {
        try {
            JSONObject jsonObject = new JSONObject(FileUtil.readFromFile(getPath()));
            fromJSON(jsonObject);
        } catch (JSONException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }


    @Override
    public JSONObject toJSON()
            throws JSONException
    {
        JSONObject rootConfig = new JSONObject();
        rootConfig.put(JSON_NAME_KEY, getName());
        rootConfig.put(JSON_TYPE_KEY, getType());
        rootConfig.put(JSON_MAXLEVEL_KEY, getMaxZoom());
        rootConfig.put(JSON_MINLEVEL_KEY, getMinZoom());
        rootConfig.put(JSON_VISIBILITY_KEY, isVisible());
        return rootConfig;
    }


    @Override
    public void fromJSON(JSONObject jsonObject)
            throws JSONException
    {
        mLayerType = jsonObject.getInt(JSON_TYPE_KEY);
        mName = jsonObject.getString(JSON_NAME_KEY);
        if (jsonObject.has(JSON_MAXLEVEL_KEY)) {
            mMaxZoom = jsonObject.getInt(JSON_MAXLEVEL_KEY);
        } else {
            mMaxZoom = DEFAULT_MAX_ZOOM;
        }
        if (jsonObject.has(JSON_MINLEVEL_KEY)) {
            mMinZoom = jsonObject.getInt(JSON_MINLEVEL_KEY);
        } else {
            mMinZoom = DEFAULT_MIN_ZOOM;
        }

        mIsVisible = jsonObject.getBoolean(JSON_VISIBILITY_KEY);
    }


    @Override
    public synchronized void onDrawFinished(
            int id,
            float percent)
    {
        if (mParent != null && mParent instanceof ILayerView) {
            ILayerView renderer = (ILayerView) mParent;
            renderer.onDrawFinished(id, percent);
        }
    }


    @Override
    public GeoEnvelope getExtents()
    {
        return mExtents;
    }


    @Override
    public void setParent(ILayer layer)
    {
        mParent = layer;
    }


    public Context getContext()
    {
        return mContext;
    }
}
