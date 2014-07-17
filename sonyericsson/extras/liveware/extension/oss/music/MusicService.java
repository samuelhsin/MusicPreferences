/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the Sony Ericsson Mobile Communications AB nor the names
  of its contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.sonyericsson.extras.liveware.extension.oss.music;

import com.sonyericsson.extras.liveware.extension.util.Dbg;
import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;
import com.sonyericsson.extras.liveware.extension.util.widget.WidgetExtension;

/**
 * The Music Service handles the music remote widget and control.
 */
public class MusicService extends ExtensionService {

    /**
     * Create music service.
     */
    public MusicService() {
        super(MusicExtension.EXTENSION_KEY);
    }

    /**
     * {@inheritDoc}
     *
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Dbg.d("MusicService: onCreate");
    }

    /**
     * {@inheritDoc}
     *
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        Dbg.d("onDestroy");

        super.onDestroy();
    }

    /* (non-Javadoc)
     * @see com.sonyericsson.extras.liveware.aef.util.ExtensionService#getRegistrationInformation()
     */
    @Override
    protected RegistrationInformation getRegistrationInformation() {
        return new MusicRegistrationInformation(this);
    }

    @Override
    public WidgetExtension createWidgetExtension(String hostAppPackageName) {
        return new MusicWidgetExtension(this, hostAppPackageName);
    }

    @Override
    public ControlExtension createControlExtension(String hostAppPackageName) {
        return new MusicControlExtension(this, hostAppPackageName);
    }

    /* (non-Javadoc)
     * @see com.sonyericsson.extras.liveware.aef.util.ExtensionService#keepRunningWhenConnected()
     * Music service needs to be running only when control or widget is visible on screen.
     */
    @Override
    protected boolean keepRunningWhenConnected() {
        return false;
    }


}
