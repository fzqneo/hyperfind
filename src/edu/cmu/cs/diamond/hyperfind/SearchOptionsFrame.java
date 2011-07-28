/*
 *  HyperFind, an search application for the OpenDiamond platform
 *
 *  Copyright (c) 2008-2011 Carnegie Mellon University
 *  All rights reserved.
 *
 *  HyperFind is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 2.
 *
 *  HyperFind is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with HyperFind. If not, see <http://www.gnu.org/licenses/>.
 *
 *  Linking HyperFind statically or dynamically with other modules is
 *  making a combined work based on HyperFind. Thus, the terms and
 *  conditions of the GNU General Public License cover the whole
 *  combination.
 *
 *  In addition, as a special exception, the copyright holders of
 *  HyperFind give you permission to combine HyperFind with free software
 *  programs or libraries that are released under the GNU LGPL or the
 *  Eclipse Public License 1.0. You may copy and distribute such a system
 *  following the terms of the GNU GPL for HyperFind and the licenses of
 *  the other code concerned, provided that you include the source code of
 *  that other code when and as the GNU GPL requires distribution of source
 *  code.
 *
 *  Note that people who make modified versions of HyperFind are not
 *  obligated to grant this special exception for their modified versions;
 *  it is their choice whether to do so. The GNU General Public License
 *  gives permission to release a modified version without this exception;
 *  this exception also makes it possible to release a modified version
 *  which carries forward this exception.
 */

package edu.cmu.cs.diamond.hyperfind;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;

import edu.cmu.cs.diamond.opendiamond.Bundle;
import edu.cmu.cs.diamond.opendiamond.BundleFactory;
import edu.cmu.cs.diamond.opendiamond.bundle.OptionGroup;
import edu.cmu.cs.diamond.opendiamond.bundle.Option;
import edu.cmu.cs.diamond.opendiamond.bundle.BooleanOption;
import edu.cmu.cs.diamond.opendiamond.bundle.StringOption;
import edu.cmu.cs.diamond.opendiamond.bundle.NumberOption;
import edu.cmu.cs.diamond.opendiamond.bundle.ChoiceOption;
import edu.cmu.cs.diamond.opendiamond.bundle.Choice;

public class SearchOptionsFrame extends JFrame {

    private final List<ChangeListener> listeners =
            new ArrayList<ChangeListener>();

    private final JComponent content;

    private final StringField instanceNameField;

    private final ArrayList<OptionField> optionFields = new
            ArrayList<OptionField>();

    private int currentRow;

    public SearchOptionsFrame(String searchName, String instanceName,
            List<OptionGroup> options) {
        super("Edit " + searchName);

        setResizable(false);
        content = (JComponent) getContentPane();
        content.setLayout(new GridBagLayout());

        final SearchOptionsFrame frame = this;

        // Close button
        JButton close_button = new JButton("Close");
        close_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
            }
        });
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = currentRow++;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(2, 2, 2, 2);
        content.add(close_button, c);

        // Search name
        StringOption opt = new StringOption();
        opt.setDisplayName("Search name");
        opt.setDefault(instanceName);
        instanceNameField = new StringField(this, opt);
        addField(instanceNameField);

        // Options
        for (OptionGroup group : options) {
            addSeparator(group.getDisplayName());
            for (Option option : group.getOptions()) {
                OptionField field;
                if (option instanceof BooleanOption) {
                    field = new BooleanField(this, (BooleanOption) option);
                } else if (option instanceof StringOption) {
                    field = new StringField(this, (StringOption) option);
                } else if (option instanceof NumberOption) {
                    field = new NumberField(this, (NumberOption) option);
                } else if (option instanceof ChoiceOption) {
                    field = new ChoiceField(this, (ChoiceOption) option);
                } else {
                    throw new IllegalArgumentException("Unknown option type");
                }
                addField(field);
                optionFields.add(field);
            }
        }

        pack();
    }

    private void addSeparator(String label) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c;

        if (label != null) {
            // left border
            c = new GridBagConstraints();
            c.gridy = 0;
            c.ipadx = 15;  // half the desired width
            p.add(new JSeparator(), c);

            // label
            c = new GridBagConstraints();
            c.gridy = 0;
            c.insets = new Insets(0, 4, 0, 4);
            p.add(new JLabel(label), c);
        }

        // right border
        c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        p.add(new JSeparator(), c);

        // add to window
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = currentRow++;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        if (label == null) {
            // simple separator between search name and options; don't use
            // pronounced section break
            c.insets = new Insets(3, 0, 3, 0);
        } else {
            c.insets = new Insets(10, 0, 0, 0);
        }
        content.add(p, c);
    }

    private void addField(OptionField field) {
        // Add label
        JLabel l = new JLabel(field.getDisplayName() + ":");
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = currentRow;
        c.insets = new Insets(2, 2, 2, 2);
        c.anchor = GridBagConstraints.LINE_START;
        content.add(l, c);

        // Add enable checkbox
        if (field.getEnableToggle() != null) {
            c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = currentRow;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(2, 2, 2, 2);
            c.anchor = GridBagConstraints.LINE_START;
            content.add(field.getEnableToggle(), c);
        }

        // Add field
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = currentRow;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(2, 2, 2, 2);
        c.anchor = GridBagConstraints.LINE_START;
        content.add(field.getComponent(), c);

        // Update state
        currentRow++;
        pack();
    }

    private abstract static class OptionField {

        private final Option option;

        private JCheckBox enable = null;

        private String valueIfDisabled = null;

        protected OptionField(Option option) {
            this.option = option;
        }

        protected void configureEnableToggle(
                final SearchOptionsFrame frame, Boolean initiallyEnabled,
                String valueIfDisabled, final List<JComponent> components) {
            if (initiallyEnabled != null) {
                enable = new JCheckBox();
                boolean enabled = initiallyEnabled.booleanValue();
                this.enable.setSelected(enabled);
                for (JComponent c : components) {
                    c.setEnabled(enabled);
                }
                this.valueIfDisabled = valueIfDisabled;

                final OptionField f = this;
                this.enable.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        for (JComponent c : components) {
                            c.setEnabled(f.isEnabled());
                        }
                        frame.fireChangeEvent();
                    }
                });
            }
        }

        public JCheckBox getEnableToggle() {
            return enable;
        }

        private boolean isEnabled() {
            return enable == null || enable.getSelectedObjects() != null;
        }

        public String getDisplayName() {
            return option.getDisplayName();
        }

        public String getName() {
            return option.getName();
        }

        public String getValue() {
            if (isEnabled()) {
                return getEnabledValue();
            } else {
                return valueIfDisabled;
            }
        }

        public abstract JComponent getComponent();

        protected abstract String getEnabledValue();
    }

    private static class BooleanField extends OptionField {

        private final BooleanOption option;

        private final JCheckBox checkbox;

        public BooleanField(final SearchOptionsFrame frame,
                BooleanOption option) {
            super(option);
            this.option = option;

            checkbox = new JCheckBox();
            checkbox.setSelected(option.isDefault());
            checkbox.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    frame.fireChangeEvent();
                }
            });
        }

        @Override
        public JComponent getComponent() {
            return checkbox;
        }

        @Override
        protected String getEnabledValue() {
            return (checkbox.getSelectedObjects() != null) ? "true" : "false";
        }
    }

    private static class StringField extends OptionField {

        private final StringOption option;

        private final JTextComponent field;

        private final JComponent component;

        private final int SINGLE_FIELD_WIDTH = 15;

        public StringField(final SearchOptionsFrame frame,
                StringOption option) {
            super(option);
            this.option = option;

            if (option.isMultiLine()) {
                field = new JTextArea(option.getDefault(), option.getHeight(),
                        option.getWidth());
                component = new JScrollPane(field);
            } else {
                field = new JTextField(option.getDefault(),
                        SINGLE_FIELD_WIDTH);
                component = field;
            }

            field.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    frame.fireChangeEvent();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    frame.fireChangeEvent();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    frame.fireChangeEvent();
                }
            });

            configureEnableToggle(frame, option.isInitiallyEnabled(),
                    option.getDisabledValue(),
                    Arrays.asList((JComponent) field));
        }

        @Override
        public JComponent getComponent() {
            return component;
        }

        @Override
        protected String getEnabledValue() {
            return field.getText();
        }
    }

    private static class NumberField extends OptionField {

        private final NumberOption option;

        private final JPanel panel;

        private final JSpinner spinner;

        private final JSlider slider;

        private final int sliderMin;

        private final int sliderMax;

        private final double step;

        private static final int SLIDER_DEFAULT_MIN = 0;

        private static final int SLIDER_DEFAULT_MAX = 100;

        private static final int FIELD_WIDTH = 8;

        public NumberField(final SearchOptionsFrame frame,
                NumberOption option) {
            super(option);
            this.option = option;

            panel = new JPanel(new GridBagLayout());
            this.step = option.getStep();
            Double min = option.getMin();
            Double max = option.getMax();
            Double defl = new Double(option.getDefault());

            // Normalize parameters
            if (min != null && defl.compareTo(min) < 0) {
                defl = min;
            } else if (max != null && defl.compareTo(max) > 0) {
                defl = max;
            }
            if (min != null) {
                sliderMin = (int) (min.doubleValue() / step);
            } else {
                sliderMin = SLIDER_DEFAULT_MIN;
            }
            if (max != null) {
                sliderMax = (int) (max.doubleValue() / step);
            } else {
                sliderMax = SLIDER_DEFAULT_MAX;
            }

            // Create spinner
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(defl,
                    min, max, new Double(step));
            spinner = new JSpinner(spinnerModel);
            ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().
                    setColumns(FIELD_WIDTH);

            // Create slider
            slider = new JSlider(sliderMin, sliderMax, sliderIndex(defl));
            slider.setPaintLabels(false);
            slider.setPaintTicks(false);
            slider.setSnapToTicks(false);

            // Add listeners.  The spinner is the master and the slider is
            // the slave.
            spinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int newIndex = sliderIndex((Double) spinner.getValue());
                    if (slider.getValue() != newIndex) {
                        slider.setValue(newIndex);
                    }
                    frame.fireChangeEvent();
                }
            });
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int newIndex = slider.getValue();
                    if (newIndex != sliderIndex((Double) spinner.getValue())) {
                        spinner.setValue(new Double(newIndex * step));
                    }
                }
            });

            // Create enable checkbox
            configureEnableToggle(frame, option.isInitiallyEnabled(),
                    string(option.getDisabledValue()),
                    Arrays.asList((JComponent) spinner, slider));

            // Add to the panel
            panel.add(spinner);
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(0, 8, 0, 0);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            panel.add(slider, c);
        }

        private int sliderIndex(Double value) {
            int ret = (int) (value.doubleValue() / step);
            ret = Math.min(ret, sliderMax);
            ret = Math.max(ret, sliderMin);
            return ret;
        }

        @Override
        public JComponent getComponent() {
            return panel;
        }

        @Override
        protected String getEnabledValue() {
            return string(((Double) spinner.getValue()).doubleValue());
        }

        private String string(double d) {
            int i = (int) d;
            if (d == i) {
                // Avoid trailing .0 if possible
                return Integer.toString(i);
            } else {
                return Double.toString(d);
            }
        }
    }

    private static class ChoiceField extends OptionField {

        private final ChoiceOption option;

        private final JComboBox comboBox;

        private final Choice[] choices;

        public ChoiceField(final SearchOptionsFrame frame,
                ChoiceOption option) {
            super(option);
            this.option = option;
            this.choices = option.getChoices().toArray(new Choice[0]);

            comboBox = new JComboBox();
            for (int i = 0; i < choices.length; i++) {
                comboBox.addItem(makeEntry(choices[i].getDisplayName()));
                if (i == 0 || choices[i].isDefault()) {
                    comboBox.setSelectedIndex(i);
                }
            }
            comboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.fireChangeEvent();
                }
            });

            configureEnableToggle(frame, option.isInitiallyEnabled(),
                    option.getDisabledValue(),
                    Arrays.asList((JComponent) comboBox));
        }

        // avoid adding the same value to the JComboBox twice
        private static Object makeEntry(final String str) {
            return new Object() {
                @Override
                public String toString() {
                    return str;
                }
            };
        }

        @Override
        public JComponent getComponent() {
            return comboBox;
        }

        @Override
        protected String getEnabledValue() {
            return choices[comboBox.getSelectedIndex()].getValue();
        }
    }

    public Map<String, String> getOptionMap() {
        Map<String, String> ret = new HashMap<String, String>();
        for (OptionField opt : optionFields) {
            ret.put(opt.getName(), opt.getValue());
        }
        return ret;
    }

    public String getInstanceName() {
        return instanceNameField.getValue();
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChangeEvent() {
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(ev);
        }
    }

    public static void main(final String args[]) throws FileNotFoundException,
            UnsupportedEncodingException, IOException {
        if (args.length != 1) {
            System.out.println("Usage: " +
                    SearchOptionsFrame.class.getName() + " bundle");
            System.exit(1);
        }

        List<File> noFiles = Collections.emptyList();
        final Bundle bundle = new BundleFactory(noFiles).getBundle(
                new File(args[0]));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SearchOptionsFrame fr = null;
                try {
                    fr = new SearchOptionsFrame(bundle.getDisplayName(),
                            "untitled", bundle.getOptions());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                fr.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentHidden(ComponentEvent e) {
                        SearchOptionsFrame fr = (SearchOptionsFrame)
                                e.getSource();
                        System.out.println("Instance: " +
                                fr.getInstanceName());
                        System.out.println("Option map:");
                        Map<String, String> optionMap = fr.getOptionMap();
                        for (String name : optionMap.keySet()) {
                            System.out.println(name + "\t" +
                                    optionMap.get(name));
                        }
                        System.exit(0);
                    }
                });
                fr.setVisible(true);
            }
        });
    }
}
