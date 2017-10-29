import React from "react";
import ReactDom from "react-dom";
import { connect } from "react-redux";
import PropTypes from "prop-types";
import { isJson } from "../../common/jsUtil.js";
import { createGearHUDComponent } from "../../common/gearHUDComponent.jsx";

class SimpleContent extends React.Component {
  constructor(props) {
    super(props)
  }

  getData() {
    if (!isJson(this.props.telemetryData)) {
      return <div></div>;
    }

    const carStateData = this.props.telemetryData.carStateData; 

    // const gearHUDComponent = createGearHUDComponent({
      // cx: 500,
      // cy: 500,
      // radius: 500,
      // gear: carStateData.gear,
      // speed: carStateData.speed,
      // rpm: carStateData.rpm,
      // maxRpm: carStateData.maxRpm,
      // throttle: carStateData.throttle,
      // brake: carStateData.brake,
      // clutch: carStateData.clutch,
      // isMeter: this.props.isMeter
    // });

    return (
      <svg viewBox="0 0 1000 1000">
      </svg>
    );
  }

  render() {
    return this.getData();
  }
}

SimpleContent.propTypes = {
  telemetryData: PropTypes.object.isRequired,
  isMeter: PropTypes.bool.isRequired
};

const mapStateToProps = state => {
  return {
    telemetryData: state.telemetryData,
    isMeter: state.options.isMeter
  };
};

const SimpleContainer = connect(
  mapStateToProps
)(SimpleContent);

export default SimpleContainer;
