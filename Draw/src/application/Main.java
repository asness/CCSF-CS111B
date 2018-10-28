package application;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Main extends Application {
	/* Some dimensions */
	private double canvasWidth = 300;
	private double canvasHeight = 300;
	private double paintBlobRadius = 5;

	/* GUI objects */
	private Text penIsDrawingText; 
	private Rectangle canvas;
	private RadioButton redButton, blueButton, yellowButton, eraserButton;
	private Button clearButton, funButton;
	private HBox buttonBox;
	private Scene scene;

	/* Use groups to organize the paint blobs and canvas:
	 * - GUIGroup
	 *   - penIsDrawingText
	 *   - buttonBox
	 *   - canvasGroup
	 *     - canvas
	 *     - paintBlobGroup
	 */
	private Group GUIGroup;
	private Group canvasGroup;
	private Group paintBlobGroup;

	/* state and logic objects */
	private boolean penIsOn = false;
	private boolean penIsColoring = true; // otherwise it's erasing
	private boolean funIsHappening = false;
	private ToggleGroup buttonToggleGroup;
	private Color currentColor;

	private void loadPenIsDrawingText() {
		penIsDrawingText = new Text();
	}

	private void refreshPenIsDrawingText() {
		penIsDrawingText.setText(penStr());
	}

	private String penStr() {
		String penActionStr;
		String funModeStr;
		if (funIsHappening)
			funModeStr = "FUN MODE!!!\n";
		else
			funModeStr = "\n"; 
		if (penIsOn) {
			if (penIsColoring)
				penActionStr = "(Coloring)";
			else
				penActionStr = "(Erasing)";
			return funModeStr + "The pen is doing something! " + penActionStr;
		}
		else {
			return funModeStr + "The pen is not doing anything.";
		}
	}

	private void loadCanvas() {
		canvas = new Rectangle(canvasWidth, canvasHeight);
		canvas.setFill(Color.WHITE);
		canvas.setStroke(Color.BLACK);
		canvas.setOnMouseClicked(this::handleCanvasMouseClick);
		canvas.setOnMouseMoved(this::handleCanvasMouseMoved);
		canvasGroup = new Group();
		canvasGroup.getChildren().add(canvas);
	}

	private void loadPaintBlobs() {
		paintBlobGroup = new Group();
		canvasGroup.getChildren().add(paintBlobGroup);
	}

	private void loadButtons() {
		// Add the color/eraser radio buttons
		redButton = new RadioButton("Red");
		blueButton = new RadioButton("Blue");
		yellowButton = new RadioButton("Yellow");
		eraserButton = new RadioButton("Eraser");
	
		// Load button logic: add the radio buttons to a toggle group.
		buttonToggleGroup = new ToggleGroup();
		redButton.setToggleGroup(buttonToggleGroup);
		blueButton.setToggleGroup(buttonToggleGroup);
		yellowButton.setToggleGroup(buttonToggleGroup);
		eraserButton.setToggleGroup(buttonToggleGroup);
		redButton.setSelected(true);
	
		// Add other buttons
		clearButton = new Button("Clear");
		clearButton.setOnAction(this::clearCanvas);
		funButton = new Button("Fun");
		funButton.setOnAction(this::switchFunMode);
	
		// arrange the buttons horizontally
		buttonBox = new HBox(redButton, blueButton, yellowButton, eraserButton, clearButton, funButton);
		buttonBox.setSpacing(20);
		buttonBox.setAlignment(Pos.CENTER);
	}

	private void loadAllGraphics() {
		loadPenIsDrawingText();
		refreshPenIsDrawingText();
		loadCanvas();
		loadPaintBlobs();
		loadButtons();
	
		/* Space manually,
		 * to preclude jittering windows
		 * due to out-of-bounds paint blobs.
		 */
		GUIGroup = new Group();
		penIsDrawingText.relocate(132, 40);
		canvasGroup.relocate(75, 75);
		buttonBox.relocate(20, 400);
		GUIGroup.getChildren().addAll(penIsDrawingText, canvasGroup, buttonBox);
	
		// add them to a scene
		scene = new Scene(GUIGroup, 450, 450);
	}

	private void handleCanvasMouseMoved(MouseEvent e) {
		double mouseXPos = e.getX();
		double mouseYPos = e.getY();

		if (penIsOn && penIsColoring) {
			PaintBlob pb = drawPaintBlob(mouseXPos, mouseYPos);
			if (funIsHappening) {
				pb.startAnimation();
			}
		}	
	}

	private void handleCanvasMouseClick(MouseEvent e) {
		switchPenState();
		refreshPenAction();
		refreshPenColor();				
		refreshPenIsDrawingText();
	}

	private void handlePaintBlobMouseEntered(MouseEvent e) {
		/* Use this method to set erasing behavior */
		PaintBlob paintBlob = (PaintBlob) e.getSource();
		if (!penIsColoring)
			paintBlobGroup.getChildren().remove(paintBlob);
	}

	private PaintBlob drawPaintBlob(double x, double y) {
		PaintBlob paintBlob = new PaintBlob(x, y, paintBlobRadius, currentColor, canvasHeight);
		// Add it to the group of paint blobs.
		paintBlobGroup.getChildren().add(paintBlob);
		// Add an onMouseEntered handler for erasing.
		paintBlob.setOnMouseEntered(this::handlePaintBlobMouseEntered);

		/* The paint blobs may obscure the underlying canvas;
		 * Therefore, add the canvas move/click handlers to each paint blob. 
		 */
		paintBlob.setOnMouseMoved(this::handleCanvasMouseMoved);
		paintBlob.setOnMouseClicked(this::handleCanvasMouseClick);
		return paintBlob;
	}

	private void switchPenState() {
		penIsOn = !penIsOn;
	}

	private void refreshPenAction() {
		Toggle t = buttonToggleGroup.getSelectedToggle();
		if (t == eraserButton)
			penIsColoring = false;
		else
			penIsColoring = true;
	}

	private void refreshPenColor() {
		Toggle t = buttonToggleGroup.getSelectedToggle();
		if (t == redButton)
			currentColor = Color.RED;
		else if (t == yellowButton)
			currentColor = Color.YELLOW;
		else if (t == blueButton)
			currentColor = Color.BLUE;
	}

	private void switchFunMode(ActionEvent event) {
		funIsHappening = !funIsHappening;
		refreshPenIsDrawingText();
		if (funIsHappening)
			startAnimation();
		else
			stopAnimation();
	}

	private void clearCanvas(ActionEvent event) {
		paintBlobGroup.getChildren().clear();
	}

	private void startAnimation() {
		for (Node n: paintBlobGroup.getChildren()) {
			PaintBlob pb = (PaintBlob) n;
			pb.startAnimation();
		}
	}

	private void stopAnimation() {
		for (Node n: paintBlobGroup.getChildren()) {
			PaintBlob pb = (PaintBlob) n;
			pb.stopAnimation();
		}		
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			loadAllGraphics();
			primaryStage.setTitle("Draw Something");
			primaryStage.setResizable(false);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
