// Copyright 2016 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.android.globalactionbarservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;

import java.util.ArrayDeque;
import java.util.Deque;

public class GlobalActionBarService extends AccessibilityService {
    FrameLayout mLayout;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e("eventddd", event.toString());

    }

    @Override
    protected boolean onGesture(int gestureId) {
        Log.e("eventddd gesture", gestureId + " tadad");

        return super.onGesture(gestureId);
    }

    @Override
    protected void onServiceConnected() {
        Log.e("eventddd", "connected");


        // Create an overlay and display the action bar
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayout = new FrameLayout(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.action_bar, mLayout);
        wm.addView(mLayout, lp);
        configurePowerButton();
        configureVolumeButton();
        configureScrollButton();
        configureSwipeButton();

    }

    @Override
    public void onInterrupt() {
        Log.e("eventddd ", " interupt called");

    }

    private void configurePowerButton() {
        Button powerButton = (Button) mLayout.findViewById(R.id.power);
        powerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
            }
        });
    }

    private void configureVolumeButton() {
        Button volumeUpButton = (Button) mLayout.findViewById(R.id.volume_up);
        volumeUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                AccessibilityNodeInfo scrollable = findScrollableNode(getRootInActiveWindow());
                if (scrollable != null) {
                    scrollable.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.getId());
                }
                clickEvent("Accounts", 2000);
                clickEvent("Add Account", 5000);
                clickEvent("Google", 8000);
           //     clickEvent("Add Account", 5000);
               // clickEvent("TURN ON", 10000);
                // clickEvent("15 seconds",5000);


                //  Log.e("TAGAA", getRootInActiveWindow().findAccessibilityNodeInfosByText("sleep").get(0).getParent().isClickable() + " ");


            }
        });
    }

    private void clickEvent(final String text, long time) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Log.e("TAG", getRootInActiveWindow().findAccessibilityNodeInfosByText(text).size() + " ");

                boolean clicked = false;
                for (int i = 0; i < getRootInActiveWindow().findAccessibilityNodeInfosByText(text).size(); i++) {
                    if (getRootInActiveWindow().findAccessibilityNodeInfosByText(text).get(i).isClickable()) {
                        clicked = true;
                        getRootInActiveWindow().findAccessibilityNodeInfosByText(text).get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    } else {
                        Log.e("TAG", "click false "+getRootInActiveWindow().findAccessibilityNodeInfosByText(text).get(i).toString());

                    }


                }

                if (!clicked) {
                    AccessibilityNodeInfo aInfo = getRootInActiveWindow().findAccessibilityNodeInfosByText(text).get(0).getParent();
                    aInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }


            }
        }, time);
    }

    private AccessibilityNodeInfo findScrollableNode(AccessibilityNodeInfo root) {
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(root);
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            if (node.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                return node;
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                deque.addLast(node.getChild(i));
            }
        }
        return null;
    }

    private void configureScrollButton() {
        Button scrollButton = mLayout.findViewById(R.id.scroll);
        scrollButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                AccessibilityNodeInfo scrollable = findScrollableNode(getRootInActiveWindow());
                if (scrollable != null) {
                    scrollable.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.getId());
                }
            }
        });
    }

    private void configureSwipeButton() {
        Button swipeButton = mLayout.findViewById(R.id.swipe);
        swipeButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Path swipePath = new Path();
                swipePath.moveTo(1000, 1000);
                swipePath.lineTo(100, 1000);
                GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));
                dispatchGesture(gestureBuilder.build(), null, null);
            }
        });
    }
}
