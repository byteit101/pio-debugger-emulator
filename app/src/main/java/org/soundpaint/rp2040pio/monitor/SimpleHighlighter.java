package org.soundpaint.rp2040pio.monitor;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.jline.reader.LineReader;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

public class SimpleHighlighter extends DefaultHighlighter {
	
	private List<Pattern> keywords;

	public void setKeywords(Collection<String> keywords) {
		this.keywords = keywords.stream().map(x -> Pattern.compile("^" + x)).toList();
	}

	@Override
	public AttributedString highlight(LineReader reader, String buffer) {
		var results = super.highlight(reader, buffer);
		for (var kw : keywords)
			results = results.styleMatches(kw, new AttributedStyle().foreground(AttributedStyle.CYAN| AttributedStyle.BRIGHT));
		return results;
	}
}
