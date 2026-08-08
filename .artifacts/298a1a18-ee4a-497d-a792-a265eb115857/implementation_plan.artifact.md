# Fix "Ask AI" Header Layout in ChatFragment

The "Ask AI" header area in `fragment_chat.xml` is currently broken. The title is incorrectly using `layout_weight="1"` in a vertical layout, causing it to take up half the screen, and the action buttons (+ New, History) are stacked vertically instead of being aligned horizontally with the title.

## Proposed Changes

### [Layouts]

#### [MODIFY] [fragment_chat.xml](file:///C:/Users/DELL/AndroidStudioProjects/Scanlern/app/src/main/res/layout/fragment_chat.xml)
- Create a horizontal header `LinearLayout` to contain:
    - `btnChatHistory` (ImageView) on the left.
    - "Ask AI" (TextView) in the center (using `layout_weight="1"`).
    - `btnNewChat` (TextView) on the right.
- Remove `layout_weight="1"` from the "Ask AI" TextView and set it to the container if needed, or simply use `wrap_content` height for the header.
- Ensure the header has proper padding and alignment.

## Verification Plan

### Automated Tests
- Build the project using `./gradlew :app:assembleDebug` to ensure no regressions.

### Manual Verification
- Deploy to device and verify that the "Ask AI" title, "+ New" button, and History icon are correctly aligned in a single horizontal row at the top.
