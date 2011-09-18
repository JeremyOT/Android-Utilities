package org.ext.utilities;

import org.xml.sax.XMLReader;

import android.text.Editable;
import android.text.Html;
import android.text.Html.TagHandler;
import android.text.Spanned;

public class HtmlUtilities {

    /**
     * This method is a more complete wrapper around the native Html.fromHtml() method that will
     * handle tags not in the limited default set of what is supported. This should be used instead
     * of Html.fromHtml() wherever unsupported tags may be included in the input text.
     * 
     * Note: Like Html.fromHtml(), unsupported tags will not cause an error. They will simply be
     * stripped from the string. This can be a useful side effect.
     * 
     * @param html
     *            Html formatted text to convert to a Spanned object.
     * @return A Spanned that can be used to display the text to the screen (e.g.:
     *         TextView.setText(Spanned, BufferType.SPANNABLE))
     */
    public static Spanned fromHtml(String html) {
        return Html.fromHtml(html, null, new TagHandler() {
            public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
                if (tag.equalsIgnoreCase("li")) {
                    if (opening)
                        output.append("\u2022 ");
                    else
                        output.append("\n");
                }
            }
        });
    }

    private HtmlUtilities() {
        // Hide constructor
    }
}
