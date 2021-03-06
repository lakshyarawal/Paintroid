/**
 *  Paintroid: An image manipulation application for Android.
 *  Copyright (C) 2010-2015 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.paintroid.test.junit.tools;

import static org.catrobat.paintroid.test.utils.PaintroidAsserts.assertPaintEquals;
import static org.catrobat.paintroid.test.utils.PaintroidAsserts.assertPathEquals;

import java.util.ArrayList;
import java.util.List;

import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.command.implementation.BaseCommand;
import org.catrobat.paintroid.command.implementation.PathCommand;
import org.catrobat.paintroid.command.implementation.PointCommand;
import org.catrobat.paintroid.dialog.colorpicker.ColorPickerDialog;
import org.catrobat.paintroid.dialog.colorpicker.ColorPickerDialog.OnColorPickedListener;
import org.catrobat.paintroid.listener.BrushPickerView;
import org.catrobat.paintroid.test.junit.stubs.PathStub;
import org.catrobat.paintroid.test.utils.PrivateAccess;
import org.catrobat.paintroid.tools.Tool;
import org.catrobat.paintroid.tools.Tool.StateChange;
import org.catrobat.paintroid.tools.ToolType;
import org.catrobat.paintroid.tools.implementation.BaseTool;
import org.catrobat.paintroid.tools.implementation.DrawTool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.test.annotation.UiThreadTest;

import static org.junit.Assert.*;

public class DrawToolTests extends BaseToolTest {

	public DrawToolTests() {
		super();
	}

	@UiThreadTest
	@Override
	@Before
	public void setUp() throws Exception {
        mToolToTest = new DrawTool(this.getActivity(), ToolType.BRUSH);
		super.setUp();
	}

	@UiThreadTest
    @Override
	@After
    public void tearDown() throws Exception {
        mToolToTest = null;
        mPaint = null;
        mCommandManagerStub = null;
        getActivity().finish();
        super.tearDown();
    }

	@UiThreadTest
    @Test
	public void testShouldReturnCorrectToolType() {
		ToolType toolType = mToolToTest.getToolType();

		assertEquals(ToolType.BRUSH, toolType);
	}

	@UiThreadTest
	@Test
	public void testShouldReturnPaint() throws NoSuchFieldException, IllegalAccessException {
		mToolToTest.setDrawPaint(this.mPaint);
		Paint drawPaint = (Paint) PrivateAccess.getMemberValue(BaseTool.class, mToolToTest, "mBitmapPaint");
		assertEquals(this.mPaint.getColor(), drawPaint.getColor());
		assertEquals(this.mPaint.getStrokeWidth(), drawPaint.getStrokeWidth(), Double.MIN_VALUE);
		assertEquals(this.mPaint.getStrokeCap(), drawPaint.getStrokeCap());
		assertEquals(this.mPaint.getShader(), drawPaint.getShader());
	}

	@UiThreadTest
	@Test
	public void testShouldMovePathOnDownEvent() throws NoSuchFieldException, IllegalAccessException {
		PointF event = new PointF(0, 0);
		PathStub pathStub = new PathStub();
		PrivateAccess.setMemberValue(DrawTool.class, mToolToTest, "pathToDraw", pathStub);

		boolean returnValue = mToolToTest.handleDown(event);

		assertTrue(returnValue);
		assertEquals(1, pathStub.getCallCount("moveTo"));
		List<Object> arguments = pathStub.getCall("moveTo", 0);
		assertEquals(event.x, arguments.get(0));
		assertEquals(event.y, arguments.get(1));
	}

	@UiThreadTest
	@Test
	public void testShouldNotAddCommandOnDownEvent() {
		PointF event = new PointF(0, 0);

		boolean returnValue = mToolToTest.handleDown(event);

		assertTrue(returnValue);
		assertEquals(0, mCommandManagerStub.getCallCount("commitCommandToLayer"));
	}

	@UiThreadTest
	@Test
	public void testShouldNotStartPathIfNoCoordinateOnDownEvent() throws NoSuchFieldException, IllegalAccessException {
		PathStub pathStub = new PathStub();
		PrivateAccess.setMemberValue(DrawTool.class, mToolToTest, "pathToDraw", pathStub);

		boolean returnValue = mToolToTest.handleDown(null);

		assertFalse(returnValue);
		assertEquals(0, pathStub.getCallCount("reset"));
		assertEquals(0, pathStub.getCallCount("moveTo"));
	}

	@UiThreadTest
	@Test
	public void testShouldMovePathOnMoveEvent() throws NoSuchFieldException, IllegalAccessException {
		PointF event1 = new PointF(0, 0);
		PointF event2 = new PointF(5, 6);
		PathStub pathStub = new PathStub();
		PrivateAccess.setMemberValue(DrawTool.class, mToolToTest, "pathToDraw", pathStub);

		mToolToTest.handleDown(event1);
		boolean returnValue = mToolToTest.handleMove(event2);

		assertTrue(returnValue);
		assertEquals(1, pathStub.getCallCount("moveTo"));
		assertEquals(1, pathStub.getCallCount("quadTo"));
		List<Object> arguments = pathStub.getCall("quadTo", 0);
		assertEquals(event1.x, arguments.get(0));
		assertEquals(event1.y, arguments.get(1));
		assertEquals(event2.x, arguments.get(2));
		assertEquals(event2.y, arguments.get(3));
	}

	@UiThreadTest
	@Test
	public void testShouldNotAddCommandOnMoveEvent() {
		PointF event = new PointF(0, 0);

		mToolToTest.handleDown(event);
		boolean returnValue = mToolToTest.handleMove(event);

		assertTrue(returnValue);
		assertEquals(0, mCommandManagerStub.getCallCount("commitCommandToLayer"));
	}

	@UiThreadTest
	@Test
	public void testShouldNotMovePathIfNoCoordinateOnMoveEvent() throws NoSuchFieldException, IllegalAccessException {
		PointF event = new PointF(0, 0);
		PathStub pathStub = new PathStub();
		PrivateAccess.setMemberValue(DrawTool.class, mToolToTest, "pathToDraw", pathStub);

		mToolToTest.handleDown(event);
		boolean returnValue = mToolToTest.handleMove(null);

		assertFalse(returnValue);
		assertEquals(0, pathStub.getCallCount("quadTo"));
	}

	@UiThreadTest
	@Test
	public void testShouldMovePathOnUpEvent() throws NoSuchFieldException, IllegalAccessException {
		PointF event1 = new PointF(0, 0);
		PointF event2 = new PointF(MOVE_TOLERANCE, MOVE_TOLERANCE);
		PointF event3 = new PointF(MOVE_TOLERANCE * 2, -MOVE_TOLERANCE);
		PathStub pathStub = new PathStub();
		PrivateAccess.setMemberValue(DrawTool.class, mToolToTest, "pathToDraw", pathStub);

		mToolToTest.handleDown(event1);
		mToolToTest.handleMove(event2);
		boolean returnValue = mToolToTest.handleUp(event3);

		assertTrue(returnValue);
		assertEquals(1, pathStub.getCallCount("moveTo"));
		assertEquals(1, pathStub.getCallCount("quadTo"));
		assertEquals(1, pathStub.getCallCount("lineTo"));
		List<Object> arguments = pathStub.getCall("lineTo", 0);
		assertEquals(event3.x, arguments.get(0));
		assertEquals(event3.y, arguments.get(1));
	}

	@UiThreadTest
	@Test
	public void testShouldNotMovePathIfNoCoordinateOnUpEvent() throws NoSuchFieldException, IllegalAccessException {
		PointF event = new PointF(0, 0);
		PathStub pathStub = new PathStub();
		PrivateAccess.setMemberValue(DrawTool.class, mToolToTest, "pathToDraw", pathStub);

		mToolToTest.handleDown(event);
		mToolToTest.handleMove(event);
		boolean returnValue = mToolToTest.handleUp(null);

		assertFalse(returnValue);
		assertEquals(0, pathStub.getCallCount("lineTo"));
	}

	@UiThreadTest
	@Test
	public void testShouldAddCommandOnUpEvent() throws NoSuchFieldException, IllegalAccessException {
		PointF event = new PointF(0, 0);
		PointF event1 = new PointF(MOVE_TOLERANCE + 0.1f, 0);
		PointF event2 = new PointF(MOVE_TOLERANCE + 2, MOVE_TOLERANCE + 2);
		PathStub pathStub = new PathStub();
		PrivateAccess.setMemberValue(DrawTool.class, mToolToTest, "pathToDraw", pathStub);

		mToolToTest.handleDown(event);
		mToolToTest.handleMove(event1);
		boolean returnValue = mToolToTest.handleUp(event2);

		assertTrue(returnValue);
		assertEquals(1, mCommandManagerStub.getCallCount("commitCommandToLayer"));
		Command command = (Command) mCommandManagerStub.getCall("commitCommandToLayer", 0).get(1);
		assertTrue(command instanceof PathCommand);
		Path path = (Path) PrivateAccess.getMemberValue(PathCommand.class, command, "mPath");
		assertPathEquals(pathStub, path);
		Paint paint = (Paint) PrivateAccess.getMemberValue(BaseCommand.class, command, "mPaint");
		assertPaintEquals(this.mPaint, paint);
	}

	@UiThreadTest
	@Test
	public void testShouldNotAddCommandIfNoCoordinateOnUpEvent() {
		PointF event = new PointF(0, 0);

		mToolToTest.handleDown(event);
		mToolToTest.handleMove(event);
		boolean returnValue = mToolToTest.handleUp(null);

		assertFalse(returnValue);
		assertEquals(0, mCommandManagerStub.getCallCount("commitCommandToLayer"));
	}

	@UiThreadTest
	@Test
	public void testShouldAddCommandOnTabEvent() throws NoSuchFieldException, IllegalAccessException {
		PointF tab = new PointF(5, 5);

		boolean returnValue1 = mToolToTest.handleDown(tab);
		boolean returnValue2 = mToolToTest.handleUp(tab);

		assertTrue(returnValue1);
		assertTrue(returnValue2);
		assertEquals(1, mCommandManagerStub.getCallCount("commitCommandToLayer"));
		Command command = (Command) mCommandManagerStub.getCall("commitCommandToLayer", 0).get(1);
		assertTrue(command instanceof PointCommand);
		PointF point = (PointF) PrivateAccess.getMemberValue(PointCommand.class, command, "mPoint");
		assertTrue(tab.equals(point.x, point.y));
		Paint paint = (Paint) PrivateAccess.getMemberValue(BaseCommand.class, command, "mPaint");
		assertPaintEquals(this.mPaint, paint);
	}

	@UiThreadTest
	@Test
	public void testShouldAddCommandOnTabWithinTolleranceEvent() throws NoSuchFieldException, IllegalAccessException {
		PointF tab1 = new PointF(0, 0);
		PointF tab2 = new PointF(MOVE_TOLERANCE - 0.1f, 0);
		PointF tab3 = new PointF(MOVE_TOLERANCE - 0.1f, MOVE_TOLERANCE - 0.1f);

		boolean returnValue1 = mToolToTest.handleDown(tab1);
		boolean returnValue2 = mToolToTest.handleMove(tab2);
		boolean returnValue3 = mToolToTest.handleUp(tab3);

		assertTrue(returnValue1);
		assertTrue(returnValue2);
		assertTrue(returnValue3);
		assertEquals(1, mCommandManagerStub.getCallCount("commitCommandToLayer"));
		Command command = (Command) mCommandManagerStub.getCall("commitCommandToLayer", 0).get(1);
		assertTrue(command instanceof PointCommand);
		PointF point = (PointF) PrivateAccess.getMemberValue(PointCommand.class, command, "mPoint");
		assertTrue(tab1.equals(point.x, point.y));
		Paint paint = (Paint) PrivateAccess.getMemberValue(BaseCommand.class, command, "mPaint");
		assertPaintEquals(this.mPaint, paint);
	}

	@UiThreadTest
	@Test
	public void testShouldAddPathCommandOnMultipleMovesWithinTolleranceEvent() {
		PointF tab1 = new PointF(7, 7);
		PointF tab2 = new PointF(7, MOVE_TOLERANCE - 0.1f);
		PointF tab3 = new PointF(7, 7);
		PointF tab4 = new PointF(7, -MOVE_TOLERANCE + 0.1f);
		PointF tab5 = new PointF(7, 7);

		mToolToTest.handleDown(tab1);
		mToolToTest.handleMove(tab2);
		mToolToTest.handleMove(tab3);
		mToolToTest.handleMove(tab4);
		mToolToTest.handleUp(tab5);

		assertEquals(1, mCommandManagerStub.getCallCount("commitCommandToLayer"));
		Command command = (Command) mCommandManagerStub.getCall("commitCommandToLayer", 0).get(1);
		assertTrue(command instanceof PathCommand);
	}

	@UiThreadTest
	@Test
	public void testShouldRewindPathOnAppliedToBitmap() throws NoSuchFieldException, IllegalAccessException {
		PathStub pathStub = new PathStub();
		PrivateAccess.setMemberValue(DrawTool.class, mToolToTest, "pathToDraw", pathStub);

		mToolToTest.resetInternalState(StateChange.RESET_INTERNAL_STATE);

		assertEquals(1, pathStub.getCallCount("rewind"));
	}

	@UiThreadTest
	@Test
	public void testShouldReturnBlackForForTopParameterButton() throws NoSuchFieldException, IllegalAccessException {
		int color = getAttributeButtonColor();
		assertEquals(Color.BLACK, color);
	}

	@UiThreadTest
	@Test
	public void testShouldChangePaintFromColorPicker() throws NoSuchFieldException, IllegalAccessException {
		//mToolToTest = new DrawTool(getActivity(), ToolType.BRUSH);
		mToolToTest.setDrawPaint(mPaint);
		ColorPickerDialog colorPicker = ColorPickerDialog.getInstance();
		@SuppressWarnings("unchecked")
		ArrayList<OnColorPickedListener> colorPickerListener = (ArrayList<OnColorPickedListener>) PrivateAccess
				.getMemberValue(ColorPickerDialog.class, colorPicker, "mOnColorPickedListener");

		for (OnColorPickedListener onColorPickedListener : colorPickerListener) {
			onColorPickedListener.colorChanged(Color.RED);
			// check if colorpicker listener is a tool, can also be color Button
			if (onColorPickedListener instanceof Tool)
				assertEquals(Color.RED, mToolToTest.getDrawPaint().getColor());
		}

	}

	@UiThreadTest
	@Test
	public void testShouldChangePaintFromBrushPicker() throws NoSuchFieldException, IllegalAccessException {
		mToolToTest.setDrawPaint(this.mPaint);
		BrushPickerView brushPicker = BrushPickerView.getInstance();
		@SuppressWarnings("unchecked")
		ArrayList<BrushPickerView.OnBrushChangedListener> brushPickerListener = (ArrayList<BrushPickerView.OnBrushChangedListener>) PrivateAccess
				.getMemberValue(BrushPickerView.class, brushPicker, "mBrushChangedListener");

		for (BrushPickerView.OnBrushChangedListener onBrushChangedListener : brushPickerListener) {
			onBrushChangedListener.setCap(Paint.Cap.ROUND);
			onBrushChangedListener.setStroke(15);
			assertEquals(Paint.Cap.ROUND, mToolToTest.getDrawPaint().getStrokeCap());
			assertEquals(15f, mToolToTest.getDrawPaint().getStrokeWidth(), Double.MIN_VALUE);
		}
	}

}
