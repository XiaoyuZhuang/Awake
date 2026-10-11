package com.xiaoyu.awake.uitest;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Instrumentation;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Xml;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.File;
import java.io.FileOutputStream;
import org.xmlpull.v1.XmlSerializer;

/** CI-only live snapshots; no idle wait and no instrumentation of the timer process. */
public class SnapshotInstrumentation extends Instrumentation {
    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        start();
    }

    @Override public void onStart() {
        Bundle result = new Bundle();
        File file = new File(getTargetContext().getCacheDir(), "window.xml");
        file.delete();
        try {
            AccessibilityServiceInfo info = getUiAutomation().getServiceInfo();
            info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                    | AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
            getUiAutomation().setServiceInfo(info);
            AccessibilityNodeInfo root = null;
            for (int attempt = 0; attempt < 20 && root == null; attempt++) {
                root = getUiAutomation().getRootInActiveWindow();
                if (root == null) Thread.sleep(50);
            }
            if (root == null) throw new IllegalStateException("No active accessibility window");
            try (FileOutputStream output = new FileOutputStream(file)) {
                XmlSerializer xml = Xml.newSerializer();
                xml.setOutput(output, "UTF-8");
                xml.startDocument("UTF-8", true);
                xml.startTag(null, "hierarchy");
                writeNode(xml, root);
                xml.endTag(null, "hierarchy");
                xml.endDocument();
            }
            result.putString("snapshot", file.getAbsolutePath());
            finish(0, result);
        } catch (Exception error) {
            result.putString("error", error.toString());
            finish(1, result);
        }
    }

    private static void writeNode(XmlSerializer xml, AccessibilityNodeInfo node) throws Exception {
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        xml.startTag(null, "node");
        xml.attribute(null, "text", node.getText() == null ? "" : node.getText().toString());
        xml.attribute(null, "resource-id", node.getViewIdResourceName() == null ? "" : node.getViewIdResourceName());
        xml.attribute(null, "bounds", "[" + bounds.left + "," + bounds.top + "][" + bounds.right + "," + bounds.bottom + "]");
        xml.attribute(null, "visible", Boolean.toString(node.isVisibleToUser()));
        for (int index = 0; index < node.getChildCount(); index++) {
            AccessibilityNodeInfo child = node.getChild(index);
            if (child != null) writeNode(xml, child);
        }
        xml.endTag(null, "node");
    }
}
