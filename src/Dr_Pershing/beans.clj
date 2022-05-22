(ns Dr-Pershing.beans
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string])
  (:import
   (javax.swing JFrame WindowConstants ImageIcon JPanel JScrollPane JTextArea BoxLayout JEditorPane ScrollPaneConstants SwingUtilities JDialog)
   (javax.swing JMenu JMenuItem JMenuBar KeyStroke JOptionPane JToolBar JButton JToggleButton JSplitPane JLabel JTextPane JTextField JTable JTabbedPane)
   (javax.swing DefaultListSelectionModel JCheckBox UIManager JTable)
   (javax.swing.border EmptyBorder)
   (javax.swing.table DefaultTableModel)
   (java.awt Canvas Graphics Graphics2D Shape Color Polygon Dimension BasicStroke Toolkit Insets BorderLayout)
   (java.awt.event KeyListener KeyEvent MouseListener MouseEvent ActionListener ActionEvent ComponentListener ComponentEvent)
   (javax.swing.event DocumentListener DocumentEvent ListSelectionListener ListSelectionEvent)
   (javax.swing.text SimpleAttributeSet StyleConstants JTextComponent)
   (java.awt.event  WindowListener WindowAdapter WindowEvent)
   (java.awt.geom Ellipse2D Ellipse2D$Double Point2D$Double)
   (com.formdev.flatlaf FlatLaf FlatLightLaf)
   (com.formdev.flatlaf.extras FlatUIDefaultsInspector FlatDesktop FlatDesktop$QuitResponse FlatSVGIcon)
   (com.formdev.flatlaf.util SystemInfo UIScale)
   (java.util.function Consumer)
   (java.util ServiceLoader)
   (org.kordamp.ikonli Ikon)
   (org.kordamp.ikonli IkonProvider)
   (org.kordamp.ikonli.swing FontIcon)
   (org.kordamp.ikonli.codicons Codicons)
   (net.miginfocom.swing MigLayout)
   (net.miginfocom.layout ConstraintParser LC UnitValue)
   (java.io File)
   (java.lang Runnable)
   (io.ipfs.api IPFS)
   (java.util.stream Stream)
   (java.util Base64)
   (java.io BufferedReader)
   (java.nio.charset StandardCharsets)))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defn process
  [{:keys [^JPanel jpanel-tab]
    :as opts}]
  (let [jtable (JTable.)
        jtable-scroll-pane (JScrollPane.)
        table-model (DefaultTableModel.)]

    (doto jtable
      (.setModel table-model)
      (.setRowSelectionAllowed true)
      (.setSelectionModel (doto (DefaultListSelectionModel.)
                            (.addListSelectionListener
                             (reify ListSelectionListener
                               (valueChanged [_ event]
                                 (when (not= -1 (.getSelectedRow jtable))
                                   (SwingUtilities/invokeLater
                                    (reify Runnable
                                      (run [_]
                                        #_(.setText jtext-field-frequency (.getValueAt jtable (.getSelectedRow jtable) 0)))))))))))
      #_(.setAutoCreateRowSorter true))

    (doto jtable-scroll-pane
      (.setViewportView jtable)
      (.setHorizontalScrollBarPolicy ScrollPaneConstants/HORIZONTAL_SCROLLBAR_NEVER))

    (doto jpanel-tab
      (.setLayout (MigLayout. "insets 10"))
      (.add jtable-scroll-pane "cell 0 0 3 1, width 100%"))))