package com.hickup;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class PacketCaptureService extends Service<Void> {
        private StringProperty filter = new SimpleStringProperty(this, "filter");
        public final void setFilter(String value) { filter.set(value); }
        public final String getFilter() { return filter.get(); }
        public final StringProperty filterProperty() { return filter; }

        private StringProperty networkInterfaceName = new SimpleStringProperty(this, "NetworkInterfaceName");
        public final void setNetworkInterfaceName(String value) { networkInterfaceName.set(value); }
        public final String getNetworkInterfaceName() { return networkInterfaceName.get(); }
        public final StringProperty networkInterfaceNameProperty() { return networkInterfaceName; }

        private StringProperty receiverIP = new SimpleStringProperty(this, "receiverIP");
        public final void setReceiverIP(String value) { receiverIP.set(value); }
        public final String getReceiverIP() { return receiverIP.get(); }
        public final StringProperty receiverIProperty() { return receiverIP; }

        private ObjectProperty<Data> capturedData = new SimpleObjectProperty<>(this, "capturedData");

        public final void setCapturedData(Data value) {
            capturedData.set(value);
        }

        public final Data getCapturedData() {
            return capturedData.get();
        }

        public final ObjectProperty<Data> capturedDataProperty() {
            return capturedData;
        }

        protected Task createTask() {
            final String _filter = getFilter();
            final String _networkInterfaceName = getNetworkInterfaceName();
            final String _receiverIP = getReceiverIP();

            return new PacketCaptureTask(_filter, _networkInterfaceName, _receiverIP, this);
        }
     }
