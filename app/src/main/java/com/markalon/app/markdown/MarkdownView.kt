package com.markalon.app.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.IntrinsicSize
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.markalon.app.ui.theme.CodeFontFamily
import com.markalon.app.ui.theme.LocalAccent
import com.markalon.app.ui.theme.LocalMarkalonColors
import com.markalon.app.ui.theme.MarkalonColors
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.task.list.items.TaskListItemMarker
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text as MdText
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser

private val markdownParser: Parser by lazy {
    Parser.builder()
        .extensions(
            listOf(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                TaskListItemsExtension.create(),
            )
        )
        .build()
}

private data class MdContext(
    val colors: MarkalonColors,
    val accent: Color,
    val bodyFont: FontFamily,
    val textColor: Color,
)

/** Renders [markdown] using the design's type scale and theme tokens. */
@Composable
fun MarkdownView(
    markdown: String,
    bodyFont: FontFamily,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val colors = LocalMarkalonColors.current
    val accent = LocalAccent.current
    val document = remember(markdown) { markdownParser.parse(markdown) }
    val ctx = MdContext(colors, accent, bodyFont, colors.fg1)

    Column(modifier.padding(contentPadding)) {
        renderBlocks(document, ctx)
    }
}

@Composable
private fun renderBlocks(parent: Node, ctx: MdContext) {
    var node = parent.firstChild
    while (node != null) {
        RenderBlock(node, ctx)
        node = node.next
    }
}

@Composable
private fun RenderBlock(node: Node, ctx: MdContext) {
    when (node) {
        is Heading -> RenderHeading(node, ctx)
        is Paragraph -> Text(
            text = buildInlines(node, ctx),
            style = bodyStyle(ctx),
            modifier = Modifier.padding(vertical = 5.dp),
        )
        is BulletList -> RenderList(node, ctx, ordered = false)
        is OrderedList -> RenderList(node, ctx, ordered = true)
        is BlockQuote -> RenderQuote(node, ctx)
        is FencedCodeBlock -> RenderCodeBlock(node.literal, ctx)
        is IndentedCodeBlock -> RenderCodeBlock(node.literal, ctx)
        is ThematicBreak -> Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
                .height(1.dp)
                .background(ctx.colors.border)
        )
        is TableBlock -> RenderTable(node, ctx)
        is HtmlBlock -> Text(node.literal, style = bodyStyle(ctx).copy(fontFamily = CodeFontFamily))
        else -> renderBlocks(node, ctx)
    }
}

@Composable
private fun RenderHeading(node: Heading, ctx: MdContext) {
    val (size, top, bottom, color, ls) = when (node.level) {
        1 -> HeadingSpec(25f, 16.dp, 8.dp, ctx.colors.fg1, (-0.01f))
        2 -> HeadingSpec(20f, 14.dp, 6.dp, ctx.colors.fg1, 0f)
        3 -> HeadingSpec(16.5f, 12.dp, 5.dp, ctx.colors.fg1, 0f)
        else -> HeadingSpec(14.5f, 10.dp, 4.dp, ctx.colors.fg2, 0f)
    }
    Text(
        text = buildInlines(node, ctx),
        style = TextStyle(
            fontFamily = ctx.bodyFont,
            fontWeight = FontWeight.Bold,
            fontSize = size.sp,
            lineHeight = (size * 1.25f).sp,
            letterSpacing = ls.em,
            color = color,
        ),
        modifier = Modifier.padding(top = top, bottom = bottom),
    )
}

private data class HeadingSpec(
    val size: Float,
    val top: androidx.compose.ui.unit.Dp,
    val bottom: androidx.compose.ui.unit.Dp,
    val color: Color,
    val letterSpacing: Float,
)

@Composable
private fun RenderList(list: Node, ctx: MdContext, ordered: Boolean) {
    Column(Modifier.padding(vertical = 4.dp)) {
        var item = list.firstChild
        var index = 1
        while (item != null) {
            if (item is ListItem) {
                RenderListItem(item, ctx, ordered, index)
                index++
            }
            item = item.next
        }
    }
}

@Composable
private fun RenderListItem(item: ListItem, ctx: MdContext, ordered: Boolean, index: Int) {
    val taskMarker = findTaskMarker(item)
    Row(Modifier.padding(vertical = 2.dp)) {
        Box(
            modifier = Modifier.width(26.dp).padding(top = 3.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            when {
                taskMarker != null -> TaskCheckbox(checked = taskMarker.isChecked, ctx = ctx)
                ordered -> Text(
                    "$index.",
                    style = bodyStyle(ctx).copy(color = ctx.colors.fg2),
                )
                else -> Text("•", style = bodyStyle(ctx).copy(color = ctx.accent))
            }
        }
        Column(Modifier.weight(1f)) {
            renderBlocks(item, ctx)
        }
    }
}

@Composable
private fun TaskCheckbox(checked: Boolean, ctx: MdContext) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .background(
                if (checked) ctx.accent else Color.Transparent,
                RoundedCornerShape(5.dp),
            )
            .border(
                width = 1.5.dp,
                color = if (checked) ctx.accent else ctx.colors.fg3,
                shape = RoundedCornerShape(5.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

private fun findTaskMarker(item: ListItem): TaskListItemMarker? {
    val firstBlock = item.firstChild ?: return null
    val firstInline = firstBlock.firstChild ?: return null
    return firstInline as? TaskListItemMarker
}

@Composable
private fun RenderQuote(node: Node, ctx: MdContext) {
    Row(
        Modifier
            .padding(vertical = 6.dp)
            .height(IntrinsicSize.Min)
    ) {
        Box(
            Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(ctx.accent, RoundedCornerShape(2.dp))
        )
        Column(Modifier.padding(start = 14.dp)) {
            renderBlocks(node, ctx.copy(textColor = ctx.colors.fg2))
        }
    }
}

@Composable
private fun RenderCodeBlock(literal: String, ctx: MdContext) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(ctx.colors.codeBg, RoundedCornerShape(10.dp))
            .border(1.dp, ctx.colors.border, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = literal.trimEnd('\n'),
            style = TextStyle(
                fontFamily = CodeFontFamily,
                fontSize = 12.5f.sp,
                lineHeight = (12.5f * 1.55f).sp,
                color = ctx.colors.fg1,
            ),
        )
    }
}

@Composable
private fun RenderTable(table: TableBlock, ctx: MdContext) {
    Column(
        Modifier
            .padding(vertical = 8.dp)
            .border(1.dp, ctx.colors.border, RoundedCornerShape(8.dp))
    ) {
        var section = table.firstChild
        while (section != null) {
            val isHead = section is TableHead
            if (section is TableHead || section is TableBody) {
                var row = section.firstChild
                while (row != null) {
                    if (row is TableRow) {
                        TableRowView(row, ctx, isHead)
                    }
                    row = row.next
                }
            }
            section = section.next
        }
    }
}

@Composable
private fun TableRowView(row: TableRow, ctx: MdContext, header: Boolean) {
    Row(Modifier.fillMaxWidth()) {
        var cell = row.firstChild
        while (cell != null) {
            if (cell is TableCell) {
                Box(
                    Modifier
                        .weight(1f)
                        .background(if (header) ctx.colors.surface2 else Color.Transparent)
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = buildInlines(cell, ctx),
                        style = bodyStyle(ctx).copy(
                            fontSize = 13.5f.sp,
                            fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
                        ),
                    )
                }
            }
            cell = cell.next
        }
    }
}

private fun bodyStyle(ctx: MdContext) = TextStyle(
    fontFamily = ctx.bodyFont,
    fontSize = 15.5f.sp,
    lineHeight = (15.5f * 1.65f).sp,
    color = ctx.textColor,
)

private fun buildInlines(parent: Node, ctx: MdContext): AnnotatedString = buildAnnotatedString {
    appendInlineChildren(parent, ctx)
}

private fun AnnotatedString.Builder.appendInlineChildren(parent: Node, ctx: MdContext) {
    var node = parent.firstChild
    while (node != null) {
        appendInline(node, ctx)
        node = node.next
    }
}

private fun AnnotatedString.Builder.appendInline(node: Node, ctx: MdContext) {
    when (node) {
        is MdText -> append(node.literal)
        is StrongEmphasis -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            appendInlineChildren(node, ctx)
        }
        is Emphasis -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
            appendInlineChildren(node, ctx)
        }
        is Strikethrough -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
            appendInlineChildren(node, ctx)
        }
        is Code -> withStyle(
            SpanStyle(
                fontFamily = CodeFontFamily,
                background = ctx.colors.codeBg,
                fontSize = 13.sp,
            )
        ) { append(node.literal) }
        is Link -> withLink(
            LinkAnnotation.Url(
                url = node.destination ?: "",
                styles = TextLinkStyles(SpanStyle(color = ctx.accent)),
            )
        ) { appendInlineChildren(node, ctx) }
        is Image -> withStyle(SpanStyle(color = ctx.colors.fg2)) {
            appendInlineChildren(node, ctx)
        }
        is HardLineBreak -> append("\n")
        is SoftLineBreak -> append("\n")
        is HtmlInline -> append(node.literal)
        is TaskListItemMarker -> Unit // rendered as a checkbox by the list item
        else -> appendInlineChildren(node, ctx)
    }
}
