//package com.folioreader.ui.view
//
//import android.content.Context
//import android.graphics.*
//import android.graphics.drawable.shapes.RectShape
//import android.util.AttributeSet
//import android.view.View
//import com.folioreader.R
//import org.springframework.core.io.Resource
//
//
//class InterPopupShape (context: Context, attrs: AttributeSet) : View(context, attrs) {
//
//    var background: Int
//
//    init {
//        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.InterPopupShape, 0 ,0)
//        background =  typedArray.getColor(R.styleable.InterPopupShape_backgroundSplit, 0)
//    }
//
//    override fun onDraw(canvas: Canvas?) {
//        super.onDraw(canvas)
//
//        val white =Color.rgb(255, 255, 255)
//        val red = Color.rgb(255, 0, 0)
//
//        val paint = Paint()
//
//        paint.color = background
//
//        val shape = RectF()
//
//        canvas?.drawRect(0f, 0f, 300f,200f, paint)
//
//        final shadowOffset = isAbove ? triangleH : -triangleH + 32;
//
//        canvas.drawPath(
//            Path()
//                    ..addRect(Rect.fromPoints(const Offset(0, 1),
//                Offset(size.width + 10, size.height + shadowOffset + 1)))
//        ..fillType = PathFillType.evenOdd,
//        Paint()
//        ..color = const Color(0xff141414).withOpacity(0.16)
//        ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 32));
//
//        final Paint paint = Paint()
//        ..color = sky.white
//        ..strokeWidth = 1
//        ..style = PaintingStyle.fill;
//        final double width = size.width;
//        final double height = size.height;
//
//        final triLeftPos = triangleLeftPosition - rectLeftPosition;
//        final triH = isAbove ? triangleH : -triangleH;
//        final double triTopPos = isAbove ? height - 10 : 10;
//
//        final Path trianglePath = Path()
//        ..moveTo(triLeftPos, triTopPos)
//        ..lineTo(triLeftPos + triangleW / 2, triTopPos + triH)
//        ..lineTo(triLeftPos + triangleW, triTopPos)
//        ..lineTo(triLeftPos, triTopPos);
//        canvas.drawPath(trianglePath, paint);
//
//        final BorderRadius borderRadius = BorderRadius.circular(12);
//        final Rect rect = Rect.fromLTRB(0, 0, width, height);
//        final RRect outer = borderRadius.toRRect(rect);
//        canvas.drawRRect(outer, paint);
//    }
//}