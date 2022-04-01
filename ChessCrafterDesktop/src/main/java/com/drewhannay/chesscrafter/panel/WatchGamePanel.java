package com.drewhannay.chesscrafter.panel;

import javax.swing.*;

public class WatchGamePanel extends JPanel {
    /*
    public WatchGamePanel(File saveFile)
	{
		if (saveFile == null)
			return;

		GameController game = null;
		try
		{
			game = AlgebraicConverter.convert(GameBuilder.newGame(Messages.getString("PlayGamePanel.classic")), saveFile); //$NON-NLS-1$
		}
		catch (Exception e1)
		{
			try
			{
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(saveFile));
				game = (GameController) in.readObject();
				game.setIsPlayback(true);
				game.setBlackMove(false);
				in.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		if (game != null)
			setGame(game);
		else
			game = getGame();

		WatchGamePanel.mWhiteTimer = ChessTimer.createTimer(TimerTypes.NO_TIMER, null, 0, 0, false);
		WatchGamePanel.mBlackTimer = ChessTimer.createTimer(TimerTypes.NO_TIMER, null, 0, 0, true);
		mHistory = new MoveController[game.getHistory().size()];
		game.getHistory().toArray(mHistory);
		try
		{
			initComponents();
			mHistoryIndex = mHistory.length - 1;

			while (mHistoryIndex >= 0)
			{
				mHistory[mHistoryIndex].undo();
				mHistoryIndex--;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		boardRefresh(game.getBoards());
	}

	public void boardRefresh(Board[] boards)
	{
		refreshSquares(boards);

		PieceController objectivePiece = getGame().isBlackMove() ? getGame().getBlackRules().objectivePiece(true) : getGame()
				.getWhiteRules().objectivePiece(false);

		if (objectivePiece != null && objectivePiece.isInCheck())
		{
			mInCheckLabel.setVisible(true);
			if (getGame().getBlackRules().objectivePiece(true).isInCheck())
				mInCheckLabel.setBorder(BorderFactory.createTitledBorder(Messages.getString("PlayGamePanel.blackTeam"))); //$NON-NLS-1$
			else
				mInCheckLabel.setBorder(BorderFactory.createTitledBorder(Messages.getString("PlayGamePanel.whiteTeam"))); //$NON-NLS-1$

			for (PieceController piece : getGame().getThreats(objectivePiece))
				piece.getSquare().setIsThreatSquare(true);
		}
		else
		{
			mInCheckLabel.setVisible(false);
		}

		int index = 0;
		PieceController[] blackCapturedPieces = getGame().getCapturedOpposingPieces(true);
		for (int i = mWhiteCapturesJail.getMaxRow(); i >= 1; i--)
		{
			for (int j = 1; j <= mWhiteCapturesJail.getMaxCol(); j++)
			{
				if (blackCapturedPieces != null && index < blackCapturedPieces.length)
				{
					mWhiteCapturesJail.getSquare(i, j).setPiece(blackCapturedPieces[index]);
					index++;
				}
				else
				{
					mWhiteCapturesJail.getSquare(i, j).setPiece(null);
				}
				mWhiteCapturesJail.getSquare(i, j).setJailStateChanged();
			}
		}

		index = 0;
		PieceController[] whiteCapturedPieces = getGame().getCapturedOpposingPieces(false);
		for (int i = mBlackCapturesJail.getMaxRow(); i >= 1; i--)
		{
			for (int j = 1; j <= mBlackCapturesJail.getMaxCol(); j++)
			{
				if (whiteCapturedPieces != null && index < whiteCapturedPieces.length)
				{
					mBlackCapturesJail.getSquare(i, j).setPiece(whiteCapturedPieces[index]);
					index++;
				}
				else
				{
					mWhiteCapturesJail.getSquare(i, j).setPiece(null);
				}
				mBlackCapturesJail.getSquare(i, j).setJailStateChanged();
			}
		}

		mWhiteLabel.setBackground(getGame().isBlackMove() ? null : SquareJLabel.HIGHLIGHT_COLOR);
		mWhiteLabel.setForeground(getGame().isBlackMove() ? Color.black : Color.white);
		mBlackLabel.setBackground(getGame().isBlackMove() ? SquareJLabel.HIGHLIGHT_COLOR : null);
		mBlackLabel.setForeground(getGame().isBlackMove() ? Color.white : Color.black);
	}

	private static void refreshSquares(Board[] boards)
	{
		for (int k = 0; k < boards.length; k++)
		{
			for (int i = 1; i <= boards[k].getMaxRow(); i++)
			{
				for (int j = 1; j <= boards[k].getMaxCol(); j++)
					boards[k].getSquare(i, j).setStateChanged();
			}
		}
	}

	public void turn(boolean isBlackTurn)
	{
		if (mWhiteTimer != null && mBlackTimer != null)
		{
			(!isBlackTurn ? mWhiteTimer : mBlackTimer).startTimer();
			(isBlackTurn ? mWhiteTimer : mBlackTimer).stopTimer();
		}
	}

	private JPanel createGrid(Board board, boolean isJail)
	{
		final JPanel gridPanel = new JPanel();

		gridPanel.setLayout(new GridLayout(board.numRows() + 1, board.numCols()));
		gridPanel.setPreferredSize(new Dimension((board.numCols() + 1) * 48, (board.numRows() + 1) * 48));

		int numberOfRows = board.numRows();
		int numOfColumns = board.numCols();
		for (int i = numberOfRows; i > 0; i--)
		{
			if (!isJail)
			{
				JLabel label = new JLabel("" + i); //$NON-NLS-1$
				label.setHorizontalAlignment(SwingConstants.CENTER);
				gridPanel.add(label);
			}

			for (int j = 1; j <= numOfColumns; j++)
				gridPanel.add(new SquareJLabel(board.getSquare(i, j)));

		}
		if (!isJail)
		{
			for (int k = 0; k <= numOfColumns; k++)
			{
				if (k != 0)
				{
					JLabel label = new JLabel("" + (char) (k - 1 + 'A')); //$NON-NLS-1$
					label.setHorizontalAlignment(SwingConstants.CENTER);
					gridPanel.add(label);
				}
				else
				{
					gridPanel.add(new JLabel("")); //$NON-NLS-1$
				}
			}
		}
		return gridPanel;
	}

	public JMenu createMenuBar()
	{
		mOptionsMenu = new JMenu(Messages.getString("PlayGamePanel.menu")); //$NON-NLS-1$
		return mOptionsMenu;
	}

	private void initComponents() throws Exception
	{
		mInCheckLabel = new JLabel(Messages.getString("PlayGamePanel.youreInCheck")); //$NON-NLS-1$
		mInCheckLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		mInCheckLabel.setForeground(Color.RED);

		int twoBoardsGridBagOffset = 0;
		if (mOptionsMenu == null || !mOptionsMenu.isVisible())
			Driver.getInstance().setMenu(createMenuBar());

		Driver.getInstance().setOptionsMenuVisibility(false);

		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		final Board[] boards = getGame().getBoards();
		setBorder(BorderFactory.createLoweredBevelBorder());

		mInCheckLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		mInCheckLabel.setHorizontalAlignment(SwingConstants.CENTER);
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridy = 0;
		constraints.gridx = 9;
		mInCheckLabel.setVisible(false);
		add(mInCheckLabel, constraints);

		if (boards.length == 1)
		{
			constraints.gridheight = 12;
			constraints.gridy = 2;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.gridwidth = 10;
			constraints.gridheight = 10;
			constraints.insets = new Insets(10, 0, 0, 0);
			constraints.gridx = 0;

			add(createGrid(boards[0], false), constraints);
		}
		else
		{
			constraints.gridheight = 12;
			constraints.gridy = 2;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.gridwidth = 10;
			constraints.insets = new Insets(10, 0, 0, 0);
			constraints.gridx = 0;

			add(createGrid(boards[0], false), constraints);

			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.gridwidth = 10;
			constraints.insets = new Insets(10, 0, 0, 0);
			constraints.gridx = 11;
			add(createGrid(boards[1], false), constraints);

			twoBoardsGridBagOffset += 10;
		}

		final JButton nextButton = new JButton(Messages.getString("PlayGamePanel.next")); //$NON-NLS-1$
		final JButton prevButton = new JButton(Messages.getString("PlayGamePanel.previous")); //$NON-NLS-1$
		prevButton.setEnabled(false);

		nextButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				prevButton.setEnabled(true);
				if (mHistoryIndex + 1 == mHistory.length)
					return;

				try
				{
					mHistory[++mHistoryIndex].execute();
					getGame().setBlackMove(!getGame().isBlackMove());
					boardRefresh(boards);
					if (mHistoryIndex + 1 == mHistory.length)
						nextButton.setEnabled(false);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});

		prevButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				nextButton.setEnabled(true);
				if (mHistoryIndex == -1)
					return;

				try
				{
					mHistory[mHistoryIndex--].undo();
					getGame().setBlackMove(!getGame().isBlackMove());
					boardRefresh(boards);

					if (mHistoryIndex == -1)
						prevButton.setEnabled(false);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});

		mWhiteLabel = new JLabel(Messages.getString("PlayGamePanel.whiteCaps")); //$NON-NLS-1$
		mWhiteLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mWhiteLabel.setBorder(BorderFactory.createTitledBorder("")); //$NON-NLS-1$

		mBlackLabel = new JLabel(Messages.getString("PlayGamePanel.blackCaps")); //$NON-NLS-1$
		mBlackLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mBlackLabel.setBorder(BorderFactory.createTitledBorder("")); //$NON-NLS-1$

		mWhiteLabel.setOpaque(true);
		mBlackLabel.setOpaque(true);
		mWhiteLabel.setVisible(true);
		mBlackLabel.setVisible(true);

		int jailBoardSize;
		if (getGame().getWhiteTeam().size() <= 4 && getGame().getBlackTeam().size() <= 4)
		{
			jailBoardSize = 4;
		}
		else
		{
			double size = getGame().getWhiteTeam().size() > getGame().getBlackTeam().size() ? Math.sqrt(getGame().getWhiteTeam()
					.size()) : Math.sqrt(getGame().getBlackTeam().size());
			jailBoardSize = (int) Math.ceil(size);
		}

		mWhiteCapturesJail = new Board(jailBoardSize, jailBoardSize, false);
		mWhiteCapturePanel = createGrid(mWhiteCapturesJail, true);
		mWhiteCapturePanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("PlayGamePanel.capturedPieces"))); //$NON-NLS-1$
		mWhiteCapturePanel.setLayout(new GridLayout(jailBoardSize, jailBoardSize));

		mWhiteCapturePanel.setPreferredSize(new Dimension((mWhiteCapturesJail.getMaxCol() + 1) * 25,
				(mWhiteCapturesJail.getMaxRow() + 1) * 25));

		mBlackCapturesJail = new Board(jailBoardSize, jailBoardSize, false);
		mBlackCapturePanel = createGrid(mBlackCapturesJail, true);
		mBlackCapturePanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("PlayGamePanel.capturedPieces"))); //$NON-NLS-1$
		mBlackCapturePanel.setLayout(new GridLayout(jailBoardSize, jailBoardSize));

		mBlackCapturePanel.setPreferredSize(new Dimension((mBlackCapturesJail.getMaxCol() + 1) * 25,
				(mBlackCapturesJail.getMaxRow() + 1) * 25));

		// add the Black Name
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.BASELINE;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		constraints.insets = new Insets(10, 10, 10, 0);
		constraints.ipadx = 100;
		constraints.gridx = 11 + twoBoardsGridBagOffset;
		constraints.gridy = 0;
		add(mBlackLabel, constraints);

		// add the Black Jail
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.BASELINE;
		constraints.gridwidth = 3;
		constraints.gridheight = 3;
		constraints.ipadx = 0;
		constraints.insets = new Insets(0, 25, 10, 25);
		constraints.gridx = 11 + twoBoardsGridBagOffset;
		constraints.gridy = 1;
		add(mBlackCapturePanel, constraints);

		// adds the Black timer
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.BASELINE;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		constraints.ipadx = 100;
		constraints.gridx = 11 + twoBoardsGridBagOffset;
		constraints.gridy = 4;
		add(nextButton, constraints);

		// adds the White timer
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.BASELINE;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		constraints.ipadx = 100;
		constraints.gridx = 11 + twoBoardsGridBagOffset;
		constraints.gridy = 5;
		add(prevButton, constraints);

		// adds the White Jail
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.BASELINE;
		constraints.gridwidth = 3;
		constraints.gridheight = 3;
		constraints.ipadx = 0;
		constraints.gridx = 11 + twoBoardsGridBagOffset;

		// change spacing and location if there is a timer or not.
		if (ChessTimer.isNoTimer(mWhiteTimer))
		{
			constraints.gridy = 6;
			constraints.insets = new Insets(10, 25, 0, 25);
		}
		else
		{
			constraints.gridy = 7;
			constraints.insets = new Insets(0, 25, 0, 25);
		}
		add(mWhiteCapturePanel, constraints);

		// add the White Name
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.BASELINE;
		constraints.gridwidth = 3;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		constraints.insets = new Insets(10, 0, 10, 0);

		// change spacing if there is a timer
		if (ChessTimer.isNoTimer(mWhiteTimer))
		{
			constraints.gridheight = 1;
			constraints.gridy = 9;
		}
		else
		{
			constraints.gridheight = 2;
			constraints.gridy = 11;
		}
		constraints.ipadx = 100;
		constraints.gridx = 11 + twoBoardsGridBagOffset;
		add(mWhiteLabel, constraints);
	}

	public void setNextMoveMustPlacePiece(boolean nextMoveMustPlacePiece)
	{
		mNextMoveMustPlacePiece = nextMoveMustPlacePiece;
	}

	public boolean getNextMoveMustPlacePiece()
	{
		return mNextMoveMustPlacePiece;
	}

	public void setPieceToPlace(PieceController piece)
	{
		mPieceToPlace = piece;
	}

	public static void setGame(GameController game)
	{
		mGame = game;
	}

	public static GameController getGame()
	{
		return mGame;
	}

	public void resetTimers()
	{
		mWhiteTimer.reset();
		mBlackTimer.reset();
	}

	private static final long serialVersionUID = -2507232401817253688L;

	protected static boolean mNextMoveMustPlacePiece;
	protected static GameController mGame;
	protected static ChessTimer mWhiteTimer;
	protected static ChessTimer mBlackTimer;
	protected static JLabel mInCheckLabel;
	protected static JLabel mWhiteLabel;
	protected static JLabel mBlackLabel;
	protected static JPanel mWhiteCapturePanel;
	protected static JPanel mBlackCapturePanel;
	protected static Board mWhiteCapturesJail;
	protected static Board mBlackCapturesJail;
	protected static Piece mPieceToPlace;
	protected static JMenu mOptionsMenu;
	protected static MoveController[] mHistory;
	protected static int mHistoryIndex;

	public void endOfGame(Result result)
	{
	}

	public void saveGame()
	{
	}
	*/
}
