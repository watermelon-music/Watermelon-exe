/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        "surface-container-high": "#2a2a2a",
        "on-primary-fixed": "#410004",
        "on-error-container": "#ffdad6",
        "on-secondary-container": "#00431f",
        "on-tertiary-fixed": "#241919",
        "surface-container-low": "#1b1b1b",
        "on-surface-variant": "#e4beba",
        "surface": "#131313",
        "tertiary-fixed": "#f2dedd",
        "tertiary-container": "#9e8d8c",
        "secondary": "#4ae183",
        "error": "#ffb4ab",
        "on-primary-container": "#5c0008",
        "surface-container-highest": "#353535",
        "secondary-container": "#06bb63",
        "inverse-on-surface": "#303030",
        "surface-variant": "#353535",
        "on-secondary": "#003919",
        "on-tertiary": "#3a2d2d",
        "on-tertiary-container": "#332727",
        "on-primary-fixed-variant": "#930014",
        "inverse-surface": "#e2e2e2",
        "background": "#0e0e0e",
        "outline-variant": "#5b403e",
        "on-secondary-fixed-variant": "#005228",
        "error-container": "#93000a",
        "surface-dim": "#131313",
        "surface-container": "#1f1f1f",
        "primary": "#ffb3ae",
        "on-tertiary-fixed-variant": "#514343",
        "primary-container": "#ff5352",
        "primary-fixed-dim": "#ffb3ae",
        "tertiary": "#d6c2c1",
        "primary-fixed": "#ffdad7",
        "surface-container-lowest": "#0e0e0e",
        "on-background": "#e2e2e2",
        "secondary-fixed": "#6bfe9c",
        "on-primary": "#68000b",
        "surface-tint": "#ffb3ae",
        "on-surface": "#e2e2e2",
        "inverse-primary": "#ba1724",
        "tertiary-fixed-dim": "#d6c2c1",
        "outline": "#ab8986",
        "on-error": "#690005",
        "secondary-fixed-dim": "#4ae183",
        "on-secondary-fixed": "#00210c",
        "surface-bright": "#393939"
      },
      borderRadius: {
        "DEFAULT": "0.25rem",
        "lg": "0.5rem",
        "xl": "0.75rem",
        "full": "9999px"
      },
      spacing: {
        "container-padding": "24px",
        "stack-gap": "16px",
        "section-margin": "40px",
        "base": "8px",
        "inline-gap": "12px"
      },
      fontFamily: {
        "display-lg": ["Sora"],
        "headline-lg-mobile": ["Sora"],
        "label-sm": ["Space Grotesk"],
        "headline-lg": ["Sora"],
        "body-md": ["Hanken Grotesk"],
        "title-md": ["Hanken Grotesk"]
      },
      fontSize: {
        "display-lg": ["40px", { lineHeight: "48px", letterSpacing: "-1px", fontWeight: "800" }],
        "headline-lg-mobile": ["24px", { lineHeight: "32px", fontWeight: "700" }],
        "label-sm": ["12px", { lineHeight: "16px", letterSpacing: "0.5px", fontWeight: "500" }],
        "headline-lg": ["32px", { lineHeight: "40px", fontWeight: "700" }],
        "body-md": ["16px", { lineHeight: "24px", fontWeight: "400" }],
        "title-md": ["18px", { lineHeight: "24px", fontWeight: "600" }]
      }
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
    require('@tailwindcss/container-queries'),
  ],
}
