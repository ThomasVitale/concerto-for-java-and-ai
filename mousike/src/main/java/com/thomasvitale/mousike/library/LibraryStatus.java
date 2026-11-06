package com.thomasvitale.mousike.library;

import java.util.List;

/**
 * Whether the library server answers, and what it offers.
 *
 * @param tools the MCP tool names the planner may use, empty when offline
 * @param detail one sentence for a badge: the server's name and version, or why it is unreachable
 */
public record LibraryStatus(boolean online, List<String> tools, String detail) {

    static LibraryStatus offline(String reason) {
        return new LibraryStatus(false, List.of(), reason);
    }

}
